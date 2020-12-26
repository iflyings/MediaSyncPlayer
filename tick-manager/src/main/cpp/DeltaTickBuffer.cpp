//
// Created by iflyings on 20-10-10.
//
#include "app_conf.h"
#include "DeltaTickBuffer.h"


DeltaTickBuffer::DeltaTickBuffer()
{
    mDataList = new long[CACHE_SIZE];
    mAverage = 0;
    mInPos = 0;
    mDataSize = 0;
}
DeltaTickBuffer::~DeltaTickBuffer()
{
    delete [] mDataList;
}

void DeltaTickBuffer::pushData(long data)
{
    mDataList[mInPos] = data;
    mInPos = GET_NEXT(mInPos);
    if (mDataSize < CACHE_SIZE) {
        mDataSize ++;
    }
    if (mDataSize == 1) {
        mAverage = mDataList[0];
    } else if (mDataSize == 2) {
        mAverage = (mDataList[0] + mDataList[1]) / 2;
    } else {
        long min_data = mDataList[0];
        long max_data = mDataList[0];
        long sum_data = 0;
        for (int i = 0;i < mDataSize;i ++) {
            if (min_data > mDataList[i]) {
                min_data = mDataList[i];
            }
            if (max_data < mDataList[i]) {
                max_data = mDataList[i];
            }
            sum_data += mDataList[i];
        }
        mAverage = (sum_data - min_data - max_data) / (CACHE_SIZE - 2);
    }
    //LOGI("Average = %ld", mAverage);
}

void DeltaTickBuffer::reset()
{
    mAverage = 0;
    mInPos = 0;
    mDataSize = 0;
}
