# react-native-vrc-osc

<p align="center">
  <a href="./README.md">简体中文</a> | <strong>English</strong>
</p>

---

[![NPM Version](https://img.shields.io/npm/v/react-native-vrc-osc.svg?style=flat-square)](https://www.npmjs.com/package/react-native-vrc-osc)
[![MIT License](https://img.shields.io/npm/l/react-native-vrc-osc.svg?style=flat-square)](https://github.com/LastStranger/react-native-vrc-osc/blob/main/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios%20%7C%20android-blue.svg?style=flat-square)](#platform-compatibility)

A high-performance Open Sound Control (OSC) library for React Native, specifically tailored for VRChat creators and programmers. It allows you to build custom controllers, companion apps, or dashboard monitors that interact directly with VRChat via OSC.

## Features

- 🚀 **High Performance**: Native UDP OSC messaging on both iOS and Android with minimal latency.
- 💬 **VRChat Optimized**: Perfect for sending chatbox text, avatar parameters, and controller inputs.
- 📡 **Bi-directional**: Support for both sending OSC messages (Client) and receiving OSC feedback (Server, supported on both iOS and Android).
- 🛠️ **Type Safe**: First-class TypeScript support.
- ⚡ **TurboModule Support**: Fully ready for the New Architecture of React Native.

---

## Installation

```sh
npm install react-native-vrc-osc
# or
yarn add react-native-vrc-osc
```

If you are on iOS, install the pods:

```sh
cd ios && pod install
```

---

## Platform Compatibility

| Feature | Android | iOS |
| :--- | :---: | :---: |
| **OSC Client (Send)** | ✅ | ✅ |
| **OSC Server (Receive)** | ✅ | ✅ |

---

## Usage

### 1. Sending OSC Messages (Client)

To send commands or parameter updates to VRChat, initialize the client with VRChat's IP address and OSC input port (default is `9000`).

```typescript
import { createClient, sendMessage } from 'react-native-vrc-osc';
import { useEffect } from 'react';

export default function App() {
  useEffect(() => {
    // Initialize the OSC client (target IP and port)
    createClient('192.168.31.180', 9000);
  }, []);

  const sendVrcChatbox = () => {
    // Send a message to VRChat chatbox: [text, instant_trigger, play_sfx]
    sendMessage('/chatbox/input', ['Hello from React Native!', true, true]);
  };

  const updateAvatarParameter = (value: number) => {
    // Update an avatar float parameter (e.g. Horns height or intensity)
    sendMessage('/avatar/parameters/Horns', [value]);
  };

  const triggerJump = () => {
    // Trigger input action (Jump)
    sendMessage('/input/Jump', [1]);
  };
}
```

### 2. Receiving OSC Messages (Server)

To listen for avatar parameter changes or other OSC output messages from VRChat, you can run an OSC Server (default VRChat output port is `9001`) and listen to native events.

```typescript
import { createServer } from 'react-native-vrc-osc';
import { useEffect } from 'react';
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const { VrcOsc } = NativeModules;
const vrcOscEmitter = new NativeEventEmitter(VrcOsc);

export default function App() {
  useEffect(() => {
    if (Platform.OS === 'ios') {
      // Start the server to listen on port 9001
      createServer('0.0.0.0', 9001);

      // Register listener for OSC messages
      const subscription = vrcOscEmitter.addListener('GotMessage', (event) => {
        const { address, data } = event;
        console.log(`Received OSC Address: ${address}`);
        console.log(`Received OSC Data:`, data);
      });

      return () => {
        subscription.remove();
      };
    }
  }, []);
}
```

---

## VRChat OSC Protocol Reference

This library is designed to make it easy to follow the official VRChat OSC protocol:

### Chatbox
- Address: `/chatbox/input`
- Arguments: `[string text, bool instant, bool play_sfx]`
- Example: `sendMessage('/chatbox/input', ["Hello!", true, true])`

### Avatar Parameters
- Address: `/avatar/parameters/<ParameterName>`
- Arguments: `[bool/int/float value]`
- Example: `sendMessage('/avatar/parameters/Mute', [true])`

### Inputs
- Address: `/input/<ActionName>`
- Arguments: `[int button_state]` (1 for pressed, 0 for released)
- Example: `sendMessage('/input/Jump', [1])`

*For more details on available addresses, see the official [VRChat OSC Documentation](https://creators.vrchat.com/platforms/android/osc/).*

---

## Troubleshooting & FAQ

### 1. Why am I not receiving any OSC messages from VRChat on my physical iOS/Android device?

**Reason**:
By default, VRChat only sends OSC output data to `127.0.0.1:9001` (localhost on the PC). If your phone and PC are on the same Wi-Fi network, VRChat does not automatically know your phone's IP address, so the packets never reach the phone.

**Solution**:
You must override VRChat's default output IP via Steam launch options:
1. Find your phone's local IP address on Wi-Fi settings (e.g., `192.168.1.100`).
2. Open Steam, right-click **VRChat** -> **Properties** -> **General** -> find **Launch Options** at the bottom.
3. Add the following parameter (replace `YOUR_PHONE_IP` with your phone's actual IP address):
   ```sh
   --osc=9000:YOUR_PHONE_IP:9001
   ```
   *Example: `--osc=9000:192.168.1.100:9001`*
4. Restart VRChat. Move your avatar or type in the chatbox, and your phone will receive the messages instantly.

### 2. Why does the console flood with `Sending 'GotMessage' with no listeners registered` warnings after Reloading the JS bundle?

**Reason**:
During development, reloading the JS bundle resets all JS-side listeners, but the native UDP server (on Android or iOS) keeps running in the background. If VRChat is continuously sending high-frequency parameter updates, the native module continues emitting `GotMessage` events to JS, but since JS is reloading and has no active subscription yet, React Native logs this warning.

**Solution**:
* This package implements the native `invalidate` lifecycle and `hasListeners` check to automatically release resources and ignore events when there are no active JS-side subscribers, preventing or minimizing these warnings.
* When turning off the receiver in your application UI, it is recommended to call `createServer('0.0.0.0', 0)` (i.e. setting port to `0`) to explicitly command the native module to stop the socket listener and free the UDP port.

---

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and set up the development workflow.

## License

MIT

---

*Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)*
