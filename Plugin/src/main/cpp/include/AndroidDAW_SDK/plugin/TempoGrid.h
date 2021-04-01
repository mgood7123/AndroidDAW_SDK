//
// Created by matthew good on 22/11/20.
//

#ifndef AndroidDAW_SDK__Plugin_TEMPOGRID_H
#define AndroidDAW_SDK__Plugin_TEMPOGRID_H

class TempoGrid {
public:
    int beats_per_minute;
    int notes_per_bar;
    int sample_rate;
    int samples_per_bar;
    int samples_per_note;
    float notes_per_second;
    bool mapped;

    static inline void map_tempo_to_sample(TempoGrid &grid) {
        grid.samples_per_note = grid.sample_rate * 60 / grid.beats_per_minute;
        grid.samples_per_bar = grid.samples_per_note * grid.notes_per_bar;
        grid.notes_per_second = (float) grid.sample_rate / (float) grid.samples_per_note;
        grid.mapped = true;
    }

    TempoGrid(int beats_per_minute = 120, int notes_per_bar = 4, int sample_rate = 48000);

    bool sample_matches_samples_per_note(uint64_t sample);
};

#endif //AndroidDAW_SDK__Plugin_TEMPOGRID_H
