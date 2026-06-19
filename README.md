# react-native-vrc-osc

<p align="center">
  <a href="#简体中文">简体中文</a> | <a href="#english">English</a>
</p>

---

# 简体中文

[![NPM Version](https://img.shields.io/npm/v/react-native-vrc-osc.svg?style=flat-square)](https://www.npmjs.com/package/react-native-vrc-osc)
[![MIT License](https://img.shields.io/npm/l/react-native-vrc-osc.svg?style=flat-square)](https://github.com/LastStranger/react-native-vrc-osc/blob/main/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios%20%7C%20android-blue.svg?style=flat-square)](#平台兼容性)

为 VRChat 创作者和程序员量身定制的高性能 React Native OSC（Open Sound Control）库。允许您直接通过 React Native 应用，利用原生 UDP 与 VRChat 进行 OSC 通信，轻松构建自定义控制器、手势伴侣应用或状态仪表盘。

## 特性

- 🚀 **高性能**：iOS 和 Android 端均采用原生的 UDP 通信，极低延迟。
- 💬 **针对 VRChat 优化**：完美支持发送聊天框文字、化身参数（Avatar Parameters）及按键输入控制（Inputs）。
- 📡 **双向通信 (iOS)**：支持发送 OSC 消息（Client）以及接收来自 VRChat 的参数反馈（Server，目前仅限 iOS）。
- 🛠️ **类型安全**：原生提供完整的 TypeScript 类型定义。
- ⚡ **TurboModule 支持**：完美兼容 React Native 的新架构（New Architecture）。

---

## 安装方法

```sh
npm install react-native-vrc-osc
# 或者使用 yarn
yarn add react-native-vrc-osc
```

如果您是在 iOS 平台上开发，请安装 pods：

```sh
cd ios && pod install
```

---

## 平台兼容性

| 功能 | Android | iOS |
| :--- | :---: | :---: |
| **OSC 客户端 (发送)** | ✅ | ✅ |
| **OSC 服务端 (接收)** | ❌ *(规划中)* | ✅ |

---

## 使用说明

### 1. 发送 OSC 消息（客户端）

要向 VRChat 发送指令或参数更新，请先使用 VRChat 运行设备的 IP 地址和 OSC 输入端口（默认为 `9000`）初始化客户端。

```typescript
import { createClient, sendMessage } from 'react-native-vrc-osc';
import { useEffect } from 'react';

export default function App() {
  useEffect(() => {
    // 初始化 OSC 客户端（目标 IP 和 VRChat OSC 监听端口）
    createClient('192.168.31.180', 9000);
  }, []);

  const sendVrcChatbox = () => {
    // 发送文字到 VRChat 聊天框：[文字内容, 是否立即触发, 是否播放提示音]
    sendMessage('/chatbox/input', ['来自 React Native 的问候！', true, true]);
  };

  const updateAvatarParameter = (value: number) => {
    // 更新化身浮点数参数（例如角的高度或亮度）
    sendMessage('/avatar/parameters/Horns', [value]);
  };

  const triggerJump = () => {
    // 触发输入动作（跳跃）
    sendMessage('/input/Jump', [1]);
  };
}
```

### 2. 接收 OSC 消息（服务端）- 仅限 iOS

要监听来自 VRChat 的化身参数变化或其他 OSC 输出消息，可以运行 OSC 服务端（默认 VRChat 输出端口为 `9001`）并监听原生事件。

```typescript
import { createServer } from 'react-native-vrc-osc';
import { useEffect } from 'react';
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const { VrcOsc } = NativeModules;
const vrcOscEmitter = new NativeEventEmitter(VrcOsc);

export default function App() {
  useEffect(() => {
    if (Platform.OS === 'ios') {
      // 启动服务端监听本地 9001 端口
      createServer('0.0.0.0', 9001);

      // 注册原生事件监听
      const subscription = vrcOscEmitter.addListener('GotMessage', (event) => {
        const { address, data } = event;
        console.log(`收到 OSC 地址: ${address}`);
        console.log(`收到 OSC 数据:`, data);
      });

      return () => {
        subscription.remove();
      };
    }
  }, []);
}
```

---

## VRChat OSC 协议常用参考

本库的设计与 VRChat 官方 OSC 协议完全兼容：

### 聊天框 (Chatbox)
- 地址：`/chatbox/input`
- 参数：`[string text, bool instant, bool play_sfx]`
- 示例：`sendMessage('/chatbox/input', ["你好！", true, true])`

### 化身参数 (Avatar Parameters)
- 地址：`/avatar/parameters/<参数名称>`
- 参数：`[bool/int/float value]`
- 示例：`sendMessage('/avatar/parameters/Mute', [true])`

### 按键输入控制 (Inputs)
- 地址：`/input/<动作名称>`
- 参数：`[int button_state]` (1 表示按下，0 表示松开)
- 示例：`sendMessage('/input/Jump', [1])`

*更多细节及可用地址请参考 [VRChat 官方 OSC 文档](https://creators.vrchat.com/platforms/android/osc/)。*

---

# English

[![NPM Version](https://img.shields.io/npm/v/react-native-vrc-osc.svg?style=flat-square)](https://www.npmjs.com/package/react-native-vrc-osc)
[![MIT License](https://img.shields.io/npm/l/react-native-vrc-osc.svg?style=flat-square)](https://github.com/LastStranger/react-native-vrc-osc/blob/main/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios%20%7C%20android-blue.svg?style=flat-square)](#platform-compatibility)

A high-performance Open Sound Control (OSC) library for React Native, specifically tailored for VRChat creators and programmers. It allows you to build custom controllers, companion apps, or dashboard monitors that interact directly with VRChat via OSC.

## Features

- 🚀 **High Performance**: Native UDP OSC messaging on both iOS and Android with minimal latency.
- 💬 **VRChat Optimized**: Perfect for sending chatbox text, avatar parameters, and controller inputs.
- 📡 **Bi-directional (iOS)**: Support for both sending OSC messages (Client) and receiving OSC feedback (Server, iOS only).
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
