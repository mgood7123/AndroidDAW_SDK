//
// Created by matthew good on 25/11/20.
//

#ifndef AndroidDAW_SDK__Plugin_PORTUTILS2_H
#define AndroidDAW_SDK__Plugin_PORTUTILS2_H


#include <cstdint>
#include <AndroidDAW_SDK/Log/log.h>

class PortUtils2 {

public:

    struct Port {
        void * buf;
    };

    struct StereoPorts {
        struct Port * l;
        struct Port * r;
    };

    struct Ports {
        Port *outputMono = nullptr;
        bool mono = false;
        StereoPorts *outputStereo = nullptr;
        bool stereo = false;
        Port **outputMultiChannel = nullptr;
        bool multiChannel = false;
        uint32_t channelCount = 0;
        uint32_t samples = 0;
        uint32_t samplesPerChannel = 0;
        void * buffer;
    } ports;

    bool allocated = false;

    template<typename type> void allocatePorts(PortUtils2 * templatePort) {
        allocatePorts<type>(templatePort->ports.samples, templatePort->ports.channelCount);
    }

    template<typename type> void allocatePorts(PortUtils2 & templatePort) {
        allocatePorts<type>(templatePort.ports.samples, templatePort.ports.channelCount);
    }

    template<typename type> void allocatePorts(uint32_t samples, uint32_t channelCount) {
        if (allocated) return;
        if (channelCount == 0) LOGF("invalid channel count: 0");
        ports.channelCount = channelCount;
        ports.samples = samples;
        ports.samplesPerChannel = samples/channelCount;
        ports.mono = channelCount == 1;
        if (ports.mono) {
            if (ports.outputMono == nullptr) ports.outputMono = new Port();
        } else {
            ports.stereo = channelCount == 2;
            if (ports.stereo) {
                if (ports.buffer == nullptr) ports.buffer = new type[ports.samples];
                if (ports.outputStereo == nullptr) ports.outputStereo = new StereoPorts();
                if (ports.outputStereo->l == nullptr) ports.outputStereo->l = new Port();
                if (ports.outputStereo->r == nullptr) ports.outputStereo->r = new Port();
                ports.outputStereo->l->buf = ports.buffer;
                ports.outputStereo->r->buf = reinterpret_cast<type*>(ports.buffer) + ports.samplesPerChannel;
            } else {
                ports.multiChannel = true;
                if (ports.outputMultiChannel == nullptr) ports.outputMultiChannel = new Port *[channelCount];
                for (int i = 0; i < channelCount; ++i) {
                    if (ports.outputMultiChannel[i] == nullptr) ports.outputMultiChannel[i] = new Port();
                }
            }
        }
        allocated = true;
    }

    template<typename type>
    void deallocatePorts(uint32_t channelCount) {
        if (!allocated) return;
        ports.channelCount = 0;
        ports.samples = 0;
        ports.samplesPerChannel = 0;
        if (ports.outputMono != nullptr) {
            // buf is never allocated for mono
            delete ports.outputMono;
            ports.outputMono = nullptr;
            ports.mono = false;
        }
        if (ports.outputStereo != nullptr) {
            delete ports.outputStereo->l;
            delete ports.outputStereo->r;
            delete ports.outputStereo;
            ports.outputStereo = nullptr;
            delete[] reinterpret_cast<type*>(ports.buffer);
            ports.buffer = nullptr;
            ports.stereo = false;
        }
        if (ports.outputMultiChannel != nullptr) {
            for (int i = 0; i < channelCount; ++i) {
                if (ports.outputMultiChannel[i]->buf != nullptr) {
                    delete[] reinterpret_cast<type *>(ports.outputMultiChannel[i]->buf);
                    ports.outputMultiChannel[i]->buf = nullptr;
                }
                delete ports.outputMultiChannel[i];
                ports.outputMultiChannel[i] = nullptr;
            }
            delete[] ports.outputMultiChannel;
            ports.outputMultiChannel = nullptr;
            ports.multiChannel = false;
        }
        allocated = false;
    }

    template<typename type>
    void deallocatePorts() {
        deallocatePorts<type>(ports.channelCount);
    }

    template <typename type> void copyFromDataToPort(void * data, int & sampleIndex, int & totalSamples) {
        uint32_t bufIndex = 0;
        int & dataIndex = sampleIndex;
        if (dataIndex >= totalSamples) dataIndex = 0;
        for (bufIndex = 0; bufIndex < ports.samplesPerChannel; bufIndex ++) {
            reinterpret_cast<type*>(ports.outputStereo->l->buf)[bufIndex] = reinterpret_cast<type*>(data)[(dataIndex) + 0];
            reinterpret_cast<type*>(ports.outputStereo->r->buf)[bufIndex] = reinterpret_cast<type*>(data)[(dataIndex) + 1];
            dataIndex+=2;
            if (dataIndex >= totalSamples) dataIndex = 0;
        }
    }

    template <typename type> void copyFromPortToPort(PortUtils2 & portToCopyFrom) {
        for (int i = 0; i < ports.samplesPerChannel; i++) {
            reinterpret_cast<type*>(ports.outputStereo->l->buf)[i] = reinterpret_cast<type*>(portToCopyFrom.ports.outputStereo->l->buf)[i];
            reinterpret_cast<type*>(ports.outputStereo->r->buf)[i] = reinterpret_cast<type*>(portToCopyFrom.ports.outputStereo->r->buf)[i];
        }
    }

    template <typename type> void copyFromPortToData(void * data) {
        uint32_t dataIndex = 0;
        for (int i = 0; i < ports.samplesPerChannel; i++) {
            reinterpret_cast<type*>(data)[(dataIndex) + 0] = reinterpret_cast<type*>(ports.outputStereo->l->buf)[i];
            reinterpret_cast<type*>(data)[(dataIndex) + 1] = reinterpret_cast<type*>(ports.outputStereo->r->buf)[i];
            dataIndex += 2;
        }
    }

    template <typename type> void fillPortBuffer(type value) {
        memset(ports.outputStereo->l->buf, value, ports.samplesPerChannel*sizeof(type));
        memset(ports.outputStereo->r->buf, value, ports.samplesPerChannel*sizeof(type));
//        for (int i = 0; i < ports.samplesPerChannel; i++) {
//            reinterpret_cast<type*>(ports.outputStereo->l->buf)[i] = value;
//            reinterpret_cast<type*>(ports.outputStereo->r->buf)[i] = value;
//        }
    }

    template <typename type> void fillPortBuffer(type value, unsigned int samples) {
        memset(ports.outputStereo->l->buf, value, samples*sizeof(type));
        memset(ports.outputStereo->r->buf, value, samples*sizeof(type));
//        for (unsigned int i = 0; i < samples; i ++) {
//            reinterpret_cast<type*>(ports.outputStereo->l->buf)[i] = value;
//            reinterpret_cast<type*>(ports.outputStereo->r->buf)[i] = value;
//        }
    }

    template <typename type> void setPortBufferIndex(int index, type value) {
        reinterpret_cast<type *>(ports.outputStereo->l->buf)[index] = value;
        reinterpret_cast<type *>(ports.outputStereo->r->buf)[index] = value;
    }

    template <typename type> void setPortBufferIndex(int index, PortUtils2 & port) {
        reinterpret_cast<type *>(ports.outputStereo->l->buf)[index] = reinterpret_cast<type*>(ports.outputStereo->l->buf)[index];;
        reinterpret_cast<type *>(ports.outputStereo->r->buf)[index] = reinterpret_cast<type*>(ports.outputStereo->r->buf)[index];
    }

    template <typename in, typename out> static out * convert(in * inPtr, size_t len) {
        out * o = new out[len];
        for (int i = 0; i < len; ++i) o[i] = inPtr[i];
        return o;
    }
};


#endif //AndroidDAW_SDK__Plugin_PORTUTILS2_H
