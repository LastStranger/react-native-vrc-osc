import { Text, View, StyleSheet } from 'react-native';
import { createClient, sendMessage } from 'react-native-vrc-osc';
import { useEffect } from 'react';
// import { createClient, sendMessage } from 'react-native-vrc-osc';

export default function App() {
  // const [status, setStatus] = useState<boolean>(false);
  useEffect(() => {
    createClient('192.168.31.180', 9000);
    sendMessage('/chatbox/input', [`${222}(${11})哈哈哈`, true, true]);
  }, []);

  const handleChange = () => {
    // setStatus(!status);
    sendMessage('/avatar/parameters/Horns', [true]);
  };

  const handleChange2 = () => {
    // setStatus(!status);
    sendMessage('/avatar/parameters/Horns', [false]);
  };
  const handleChange3 = () => {
    // setStatus(!status);
    // sendMessage('/avatar/parameters/Horns', ["什么情况"]);
    sendMessage('/chatbox/input', [`哈哈哈`, true, true]);
    sendMessage('/input/Jump', [1]);
  };

  return (
    <View style={styles.container}>
      <Text onPress={handleChange}>11111Result:</Text>
      <Text onPress={handleChange2}>11111Result:</Text>
      <Text style={{ marginTop: 50 }} onPress={handleChange3}>
        222111666622222222211111Result:
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
