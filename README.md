# react-native-vrc-osc

<p align="center">
  <strong>简体中文</strong> | <a href="./README_EN.md">English</a>
</p>

---

[![NPM Version](https://img.shields.io/npm/v/react-native-vrc-osc.svg?style=flat-square)](https://www.npmjs.com/package/react-native-vrc-osc)
[![MIT License](https://img.shields.io/npm/l/react-native-vrc-osc.svg?style=flat-square)](https://github.com/LastStranger/react-native-vrc-osc/blob/main/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios%20%7C%20android-blue.svg?style=flat-square)](#平台兼容性)

为 VRChat 创作者和程序员量身定制的高性能 React Native OSC（Open Sound Control）库。允许您直接通过 React Native 应用，利用原生 UDP 与 VRChat 进行 OSC 通信，轻松构建自定义控制器、手势伴侣应用或状态仪表盘。

## 特性

- 🚀 **高性能**：iOS 和 Android 端均采用原生的 UDP 通信，极低延迟。
- 💬 **针对 VRChat 优化**：完美支持发送聊天框文字、化身参数（Avatar Parameters）及按键输入控制（Inputs）。
- 📡 **双向通信**：支持发送 OSC 消息（Client）以及接收来自 VRChat 的参数反馈（Server，已同时支持 iOS 和 Android）。
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
| **OSC 服务端 (接收)** | ✅ | ✅ |

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

### 2. 接收 OSC 消息（服务端）

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

## 常见问题与排查 (FAQ)

### 1. 为什么我的真机（iOS/Android 手机）接收不到来自 VRChat 的任何 OSC 消息？

**原因分析**：
VRChat 默认只向电脑本地 `127.0.0.1:9001` (localhost) 发送 OSC 输出数据。如果你的手机和电脑连接在同一个 Wi-Fi 网络中，VRChat 并不知道手机的 IP，所以数据包无法传输到手机。

**解决方案**：
你必须通过 Steam 启动参数，指定 VRChat 将 OSC 消息发送到你手机的局域网 IP 上：
1. 获取你手机在 Wi-Fi 局域网中的 IP 地址（例如 `192.168.1.100`）。
2. 在 Steam 中右键点击 **VRChat** -> **属性 (Properties)** -> **通用 (General)** -> 找到底部的 **启动选项 (Launch Options)**。
3. 输入以下参数（将 `YOUR_PHONE_IP` 替换为手机的实际 IP 地址）：
   ```sh
   --osc=9000:YOUR_PHONE_IP:9001
   ```
   *例如：`--osc=9000:192.168.1.100:9001`*
4. 重新启动 VRChat，此时走动人物或输入聊天框，手机便能实时收到消息。

### 2. 为什么刷新 App (Reload JS) 后，控制台一直报错/警告 `Sending 'GotMessage' with no listeners registered`？

**原因分析**：
在开发环境下，如果你 Reload 了 JS 包，React Native 的 JS 监听器会被重置，但在原生侧（iOS/Android）开启的 UDP 监听服务后台线程仍在运行。此时如果有高频的 VRChat OSC 参数发来，原生模块触发 `sendEvent` 推送给 JS，但 JS 端尚未重新注册监听器，因此会触发 React Native 的警告。

**解决方案**：
* 本库已在原生侧实现 `invalidate` 生命周期释放和 `hasListeners` 校验，当 JS 重载或移除所有监听器后，原生会自动关闭并忽略多余数据，极大地减少或彻底消除此警告。
* 在前端控制切换监听状态时，建议在关闭监听时调用 `createServer('0.0.0.0', 0)`（即将端口设为 `0`）来显式指示原生侧关闭并释放底层的 UDP 服务，回收端口资源。

---

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and set up the development workflow.

## License

MIT

---

*Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)*
