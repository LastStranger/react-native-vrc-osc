# react-native-vrc-osc

[![NPM Version](https://img.shields.io/npm/v/react-native-vrc-osc.svg?style=flat-square)](https://www.npmjs.com/package/react-native-vrc-osc)
[![MIT License](https://img.shields.io/npm/l/react-native-vrc-osc.svg?style=flat-square)](https://github.com/LastStranger/react-native-vrc-osc/blob/main/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios%20%7C%20android-blue.svg?style=flat-square)](#platform-compatibility)

A high-performance Open Sound Control (OSC) library for React Native, specifically tailored for VRChat creators and programmers. It allows you to build custom controllers, companion apps, or dashboard monitors that interact directly with VRChat via OSC.

## Features

- 🚀 **High Performance**: Native UDP OSC messaging on both iOS and Android.
- 💬 **VRChat Optimized**: Perfect for sending chatbox text, avatar parameters, and controller inputs.
- 📡 **Bi-directional (iOS)**: Support for both sending OSC messages (Client) and receiving OSC messages (Server, iOS only).
- 🛠️ **Type Safe**: First-class TypeScript support.
- ⚡ **TurboModule Support**: Ready for the New Architecture of React Native.

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
| **OSC Server (Receive)** | ❌ *(Planned)* | ✅ |

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

  const updateAvatarParameter = (value: float) => {
    // Update an avatar float parameter (e.g. Horns height or intensity)
    sendMessage('/avatar/parameters/Horns', [value]);
  };

  const triggerJump = () => {
    // Trigger input action (Jump)
    sendMessage('/input/Jump', [1]);
  };
}
```

### 2. Receiving OSC Messages (Server) - iOS Only

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

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and set up the development workflow.

## License

MIT

---

*Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)*
