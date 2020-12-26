//
// Created by iflyings on 20-10-10.
//

#ifndef KTVPLAYER_DELTATICKBUFFER_H
#define KTVPLAYER_DELTATICKBUFFER_H

#define CACHE_SIZE                          (8)
#define GET_NEXT(i)                         (((i) + 1) & (CACHE_SIZE - 1))

class DeltaTickBuffer {
private:
    long *mDataList;
    unsigned int mInPos;
    unsigned int mDataSize;
    long mAverage;
public:
    DeltaTickBuffer();
    ~DeltaTickBuffer();
    void pushData(long data);
    void reset();
    long getAverageData() {
        return mAverage;
    };
};

#endif //KTVPLAYER_DELTATICKBUFFER_H
