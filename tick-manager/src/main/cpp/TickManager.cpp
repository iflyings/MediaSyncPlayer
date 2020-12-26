#include "app_conf.h"
#include "DeltaTickBuffer.h"
#include <jni.h>
#include <pthread.h>
#include <unistd.h>
#include <ctime>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/eventfd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <strings.h>
#include <stdlib.h>

#define UDP_PORT                            (19864)
#define SYNC_TICK_PERIOD_US                 (200 * 1000)
#define SYNC_TICK_TIMEOUT_US                (SYNC_TICK_PERIOD_US * 5)
#define GET_TICK(p)                         (*(long*)&((p)[4]))

static uint32_t mLocalIpAddress = 0;
static int mUdpSocket = 0;
static pthread_t mRecvThreadId = 0;
static pthread_t mTickThreadId = 0;
static volatile bool isThreadRunning = false;
static volatile bool isMasterDevice = false;
static volatile bool isSyncReady = false;
static int mTimeoutCount = 0;
static long mMasterDelta = 0;

static DeltaTickBuffer mDeltaTickBuffer;

static inline long get_elapsed_realtime_us()
{
    timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return now.tv_sec * 1000000L + now.tv_nsec / 1000L;
}

static void *thread_recv_fun(void *)
{
    char buffer[16] = {0};
    struct sockaddr_in address = {0};
    int len = sizeof(address);

    srand((unsigned int)get_elapsed_realtime_us());
    while (isThreadRunning) {
        fd_set fds;
        long delay = SYNC_TICK_TIMEOUT_US * 2 + rand() % SYNC_TICK_TIMEOUT_US;
        struct timeval timeout = {
            .tv_sec = delay / 1000000,
            .tv_usec = delay - timeout.tv_sec * 1000000,
        };

        FD_ZERO(&fds);
        FD_SET(mUdpSocket, &fds);

        int ret = select(mUdpSocket+1, &fds, nullptr, nullptr, &timeout);
        if (ret > 0) {
            if (FD_ISSET(mUdpSocket,&fds)) {
                int recv_len = recvfrom(mUdpSocket, buffer, sizeof(buffer), 0, (struct sockaddr *)&address,
                                        reinterpret_cast<socklen_t *>(&len));
                if (recv_len == 8 + 4 && *((uint32_t*)&buffer[0]) == 0x0008aa5a) {
                    if (isMasterDevice && mLocalIpAddress != htonl(address.sin_addr.s_addr)) {
                        // 主设备变成从设备
                        isMasterDevice = false;
                        isSyncReady = false;
                        mDeltaTickBuffer.reset();
                        //LOGI("ip = %08x,local = %08x", address.sin_addr.s_addr, mLocalIpAddress);
                    } else if (!isMasterDevice) {
                        long tick = GET_TICK(buffer);
                        mDeltaTickBuffer.pushData(tick - get_elapsed_realtime_us());
                        if (!isSyncReady) {
                            isSyncReady = true;
                        }
                    }
                    mTimeoutCount = 0;
                }
            }
        } else if (ret == 0) {
            if (!isMasterDevice) {
                LOGI("thread recv fun mTimeoutCount = %d\n", mTimeoutCount);
                if (++ mTimeoutCount >= 3) {
                    // 从设备变成主设备
                    isMasterDevice = true;
                    isSyncReady = true;
                    mMasterDelta = mDeltaTickBuffer.getAverageData();
                    mTimeoutCount = 0;
                }
            }
        }
    }
    LOGI("thread recv over!!!");
    return nullptr;
}

static void *thread_tick_fun(void *)
{
    uint8_t buffer[] = {0x5a,0xaa,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    struct sockaddr_in broadcast = {0};
    broadcast.sin_family = AF_INET;
    broadcast.sin_port = htons(UDP_PORT);
    broadcast.sin_addr.s_addr = htonl(mLocalIpAddress | 0x000000FF); //inet_addr("192.168.1.255");
    //LOGI("broad ip = %08x, local ip = %08x", broadcast.sin_addr.s_addr, mLocalIpAddress);
    while (isThreadRunning) {
        struct timeval timeout = {
                .tv_sec = SYNC_TICK_PERIOD_US / 1000000,
                .tv_usec = SYNC_TICK_PERIOD_US - timeout.tv_sec * 1000000,
        };
        select(0, nullptr,nullptr, nullptr, &timeout);
        if (isMasterDevice) {
            long tick = get_elapsed_realtime_us() + mMasterDelta;
            GET_TICK(buffer) = tick;
            sendto(mUdpSocket, buffer, sizeof(buffer), 0, (struct sockaddr *) &broadcast, sizeof(broadcast));
        }
    }
    LOGI("thread tick over!!!");
    return nullptr;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_android_iflyings_tickmanager_TickManager_nativeOpen(JNIEnv*, jobject, jint local_ip) {
    int opt = 1;
    struct sockaddr_in addr_in = {};

    mLocalIpAddress = local_ip;

    mUdpSocket = socket(AF_INET, SOCK_DGRAM, 0);
    if (mUdpSocket <= 0) {
        goto ERR_0;
    }

    bzero(&addr_in, sizeof(addr_in));
    addr_in.sin_family = AF_INET;
    addr_in.sin_port = htons(UDP_PORT);
    addr_in.sin_addr.s_addr = htonl(INADDR_ANY);

    if (0 > bind(mUdpSocket, (struct sockaddr*)&addr_in, sizeof(addr_in))) {
        goto ERR_1;
    }

    if (0 > setsockopt(mUdpSocket, SOL_SOCKET, SO_BROADCAST, (char*) &opt, sizeof(opt))) {
        goto ERR_1;
    }

    isThreadRunning = true;
    if (0 > pthread_create(&mRecvThreadId, nullptr, thread_recv_fun, nullptr)) {
        goto ERR_1;
    }
    if (0 > pthread_create(&mTickThreadId, nullptr, thread_tick_fun, nullptr)) {
        goto ERR_1;
    }

    return 0;
ERR_1:
    close(mUdpSocket);
    mUdpSocket = 0;
ERR_0:
    return -1;
}

static long get_network_tick_us() {
    if (mLocalIpAddress == 0 || isMasterDevice) {
        return get_elapsed_realtime_us() + mMasterDelta;
    } else {
        return get_elapsed_realtime_us() + mDeltaTickBuffer.getAverageData();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_tickmanager_TickManager_nativeClose(JNIEnv*, jobject) {
    isThreadRunning = false;
    pthread_join(mRecvThreadId, nullptr);
    pthread_join(mTickThreadId, nullptr);
    close(mUdpSocket);
    mUdpSocket = 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_android_iflyings_tickmanager_TickManager_nativeIsSyncReady(JNIEnv*, jclass) {
    return mLocalIpAddress == 0 || isSyncReady;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_android_iflyings_tickmanager_TickManager_nativeIsMasterDevice(JNIEnv*, jclass) {
    return mLocalIpAddress == 0 || isMasterDevice;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_android_iflyings_tickmanager_TickManager_nativeGetNetworkTickUs(JNIEnv*, jclass) {
    return get_network_tick_us();
}

extern "C" JNIEXPORT void JNICALL
Java_com_android_iflyings_tickmanager_TickManager_nativeSleepTo(JNIEnv*, jclass, jlong tick_us) {
    long sleepUs = tick_us - get_network_tick_us();
    if (sleepUs > 0)
        usleep(sleepUs);
}