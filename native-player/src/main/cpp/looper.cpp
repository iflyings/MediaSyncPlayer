/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "looper.h"

struct looper_message;
typedef struct looper_message looper_message;

struct looper_message {
    int what;
    void *obj;
    struct list_head list_head;
};

void* looper::trampoline(void* p) {
    ((looper*)p)->loop();
    return nullptr;
}

int looper::open() {
    INIT_LIST_HEAD(&list_head);
    if (0 != sem_init(&list_available, 0, 0)) {
        return -1;
    }
    if (0 != pthread_mutex_init(&list_lock, nullptr)) {
        return -1;
    }

    running = true;
    requestExist = false;
    if (0 > pthread_create(&thread_id, nullptr, trampoline, this)) {
        running = false;
        return -1;
    }

    return 0;
}

void looper::closeWait() {
    if (running) {
        quit();
    }
}

void looper::post(int what, void *data) {
    if (requestExist) {
        return;
    }
    auto *msg = new looper_message();
    msg->what = what;
    msg->obj = data;
    pthread_mutex_lock(&list_lock);
    list_add_tail(&msg->list_head, &list_head);
    pthread_mutex_unlock(&list_lock);
    sem_post(&list_available);
}

void looper::clean() {
    clean(false);
}

void looper::clean(bool safe) {
    pthread_mutex_lock(&list_lock);
    while (!list_empty(&list_head)) {
        struct list_head *first_head = list_head.next;
        list_del(first_head);
        looper_message *msg = container_of(first_head, struct looper_message, list_head);
        if (safe) {
            handle(msg->what, msg->obj);
        }
        delete msg;
    }
    pthread_mutex_unlock(&list_lock);
}

void looper::loop() {
    while(true) {
        sem_wait(&list_available);
        if (requestExist) {
            break;
        }

        struct list_head *first_head = NULL;
        pthread_mutex_lock(&list_lock);
        if (!list_empty(&list_head)) {
            first_head = list_head.next;
            list_del(first_head);
        }
        pthread_mutex_unlock(&list_lock);

        if (first_head == NULL) {
            continue;
        }

        looper_message *msg = container_of(first_head, struct looper_message, list_head);
        handle(msg->what, msg->obj);
        delete msg;
    }
    clean(true);
}

void looper::quit() {
    requestExist = true;
    sem_post(&list_available);

    pthread_join(thread_id, NULL);
    sem_destroy(&list_available);
    pthread_mutex_destroy(&list_lock);
    running = false;
}

void looper::handle(int what, void* obj) {
}

