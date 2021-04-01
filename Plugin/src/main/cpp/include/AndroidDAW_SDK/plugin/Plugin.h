//
// Created by matthew good on 28/11/20.
//

#ifndef AndroidDAW_SDK__Plugin_PLUGIN_H
#define AndroidDAW_SDK__Plugin_PLUGIN_H

#include "PortUtils2.h"
#include "HostInfo.h"
#include "typedefs.h"
#include <string>
#include <fcntl.h>
#include <unistd.h>

class Plugin {
private:
    int PLUGIN_TYPE_NONE = 1 << 1;
public:
    enum {
        PLUGIN_CONTINUE,
        PLUGIN_STOP
    };

    int PLUGIN_TYPE_GENERATOR = 1 << 2;
    int PLUGIN_TYPE_EFFECT = 1 << 3;
    int PLUGIN_TYPE_MIXER = 1 << 4;

    int is_writing = PLUGIN_STOP;

    std::function<int()> getSampleRate;
    std::function<int()> getOutputChannelCount;
    std::function<std::string (std::string)> decode;

    // add two values and detect overflow/underflow, returning TYPE_MAX if overflow occured, and TYPE_MIN if underflow occured
    template<typename type> type PLUGIN_HELPERS_add(type TYPE_MIN, type TYPE_MAX, type lhs, type rhs, bool & overflowed, bool & underflow)
    {
        if (lhs >= 0) {
            if (TYPE_MAX - lhs < rhs) {
                overflowed = true;
                return TYPE_MAX;
            }
        }
        else {
            if (rhs < TYPE_MIN - lhs) {
                underflow = true;
                return TYPE_MIN;
            }
        }
        return lhs + rhs;
    }

    virtual int plugin_type() {
        return PLUGIN_TYPE_NONE;
    };

    std::vector<PortUtils2*> ports;

    virtual void addPort(PortUtils2 *port) {
        ports.push_back(port);
    }

    virtual void removePort(PortUtils2 *port) {
        for (auto it = ports.begin(); it != ports.end(); it++) {
            if (*it == port) {
                ports.erase(it);
                break;
            }
        }
    }

    // TODO: support file loading for sampler plugins

    virtual int write(HostInfo * hostInfo, PortUtils2 * in, Plugin * mixer, PortUtils2 * out, unsigned int samples) {
        return PLUGIN_STOP;
    };

    // SAMPLER

    void * audioData = nullptr;
    size_t audioDataSize = -1;
    int    audioDataTotalSamples = 0;

    jnk0le::Ringbuffer<int, 2> eventBuffer;

    void addEvent(int event) {
        if (eventBuffer.isFull()) eventBuffer.remove();
        eventBuffer.insert(event);
    }

    bool mIsPlaying = true;
    bool mIsLooping = true;
    int mReadSampleIndex = 0;

    virtual void loop(bool value) {
        mIsLooping = value;
    }

    virtual void startPlayback() {
        mIsPlaying = true;
    }

    virtual void pausePlayback() {
        mIsPlaying = false;
    }

    virtual void stopPlayback() {
        mIsPlaying = false;
        mReadSampleIndex = 0;
    }

    void load(const char *filename) {
        std::string path = std::string(filename);
        std::string decoded = decode(path);
        int fd;
        size_t len = 0;
        void *o = NULL;
        fd = open(decoded.c_str(), O_RDONLY);
        if (!fd) {
            LOGF("open() failure");
            return;
        }
        len = (size_t) lseek(fd, 0, SEEK_END);
        lseek(fd, 0, 0);
        if (!(o = malloc(len))) {
            int cl = close(fd);
            if (cl < 0) {
                LOGE("cannot close \"%s\", returned %d\n", filename, cl);
            }
            LOGF("failure to malloc()");
            return;
        }
        if ((read(fd, o, len)) == -1) {
            int cl = close(fd);
            if (cl < 0) {
                LOGE("cannot close \"%s\", returned %d\n", filename, cl);
            }
            LOGF("failure to read()");
            return;
        }
        int cl = close(fd);
        if (cl < 0) {
            LOGF("cannot close \"%s\", returned %d\n", filename, cl);
            return;
        }

        // file has been read into memory
        if (audioData != nullptr) {
            free(audioData);
            audioData = nullptr;
        }
        audioDataSize = len;
        audioDataTotalSamples = audioDataSize / (sizeof(float) * getOutputChannelCount());
        audioData = o;
    }

    ~Plugin() {
        if (audioData != nullptr) free(audioData);
    }

    inline bool hasAudioData() {
        return audioData != nullptr && audioDataSize != -1;
    }
};

#endif //AndroidDAW_SDK__Plugin_PLUGIN_H