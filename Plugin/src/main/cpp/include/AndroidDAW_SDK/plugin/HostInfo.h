//
// Created by matthew good on 28/11/20.
//

#ifndef AndroidDAW_SDK__Plugin_HOSTINFO_H
#define AndroidDAW_SDK__Plugin_HOSTINFO_H

#include "TempoGrid.h"
#include <AndroidDAW_SDK/midifile/MidiEventList.h>
#include <AndroidDAW_SDK/RingBuffer/ringbuffer.hpp>
#include <cstdint>

class HostInfo {

public:
    TempoGrid tempoGrid = TempoGrid(120);
    uint64_t engineSample = 0;
    void * channelRack;
    void * patternGroup;
    void * midiFile;
    smf::MidiEventList midiEventList;

    typedef jnk0le::Ringbuffer<smf::MidiEvent, 1048576> PianoRollRingBuffer;

    PianoRollRingBuffer midiInputBuffer;
    PianoRollRingBuffer midiPlaylistInputBuffer;

    template <typename T> T wrap(T sample, T min, T max) {
        return min + ((sample-min) % (max - min));
    }

    void fillMidiEvents(PianoRollRingBuffer &midiInputBuffer, TempoGrid &grid,
                        PianoRollRingBuffer &noteData,
                        unsigned int samples, uint64_t &engineSample) {
        midiInputBuffer.consumerClear();
        if (noteData.isEmpty()) return;
        int size = noteData.readAvailable();
        uint64_t wrappedStart = wrap<uint64_t>(engineSample, 0, grid.samples_per_bar);
        uint64_t wrappedEnd = wrap<uint64_t>(engineSample + samples, 0, grid.samples_per_bar);
        for (int i = 0; i < size; ++i) {
            smf::MidiEvent * midiEvent = noteData.at(i);
            if (midiEvent != nullptr) {
                if (wrappedEnd < wrappedStart) {
                    if (midiEvent->tick < wrappedEnd) {
                        if (size != 16) LOGE("OVER (valid) size = %zu, midiEvent->tick = %d, wrappedStart = %llu, wrappedEnd = %llu", size, midiEvent->tick, wrappedStart, wrappedEnd);
                        midiInputBuffer.insert(midiEvent);
                    } else {
//                        if (size != 16) LOGE("OVER (invalid) size = %zu, midiEvent->tick = %d, wrappedStart = %llu, wrappedEnd = %llu", size, midiEvent->tick, wrappedStart, wrappedEnd);
                        break;
                    }
                } else {
                    if (midiEvent->tick >= wrappedStart) {
                        if (midiEvent->tick < wrappedEnd) {
                            if (size != 16) LOGE("FILL (valid) size = %zu, midiEvent->tick = %d, wrappedStart = %llu, wrappedEnd = %llu", size, midiEvent->tick, wrappedStart, wrappedEnd);
                            midiInputBuffer.insert(midiEvent);
                        } else {
//                            if (size != 16) LOGE("FILL (invalid) size = %zu, midiEvent->tick = %d, wrappedStart = %llu, wrappedEnd = %llu", size, midiEvent->tick, wrappedStart, wrappedEnd);
                            break;
                        }
                    } else {
//                        if (size != 16) LOGE("FILL (invalid) size = %zu, midiEvent->tick = %d, wrappedStart = %llu, wrappedEnd = %llu", size, midiEvent->tick, wrappedStart, wrappedEnd);
                    }
                }
            }
        }
    }
};

#endif //AndroidDAW_SDK__Plugin_HOSTINFO_H
