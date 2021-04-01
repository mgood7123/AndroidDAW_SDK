//
// Created by matthew good on 22/11/20.
//

#include <cstdint>
#include <AndroidDAW_SDK/plugin/TempoGrid.h>

TempoGrid::TempoGrid(int beats_per_minute, int notes_per_bar, int sample_rate) {
    mapped = false;
    this->beats_per_minute = beats_per_minute;
    this->notes_per_bar = notes_per_bar;
    this->sample_rate = sample_rate;
}

bool TempoGrid::sample_matches_samples_per_note(uint64_t sample) {
    return sample == samples_per_note || (sample % samples_per_note) == 0;
}
