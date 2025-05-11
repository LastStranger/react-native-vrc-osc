# react-native-vrc-osc

react-native osc library particularly for vrchat programers

## Installation

```sh
npm install react-native-vrc-osc
pod install
```

## Usage


```js
import { createClient, sendMessage } from 'react-native-vrc-osc';

// ...

createClient('192.168.31.100', 9000);
sendMessage('/chatbox/input', ["hello world", true, true]);
```


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
