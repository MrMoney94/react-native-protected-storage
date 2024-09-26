import { useState } from 'react';
import { StyleSheet, View, Text, Pressable } from 'react-native';
import {
  setItemKS,
  getItemKS,
  removeItemKS,
  clearAll,
  setItemStorage,
  getItemStorage,
  removeItemStorage,
} from 'react-native-protected-storage';

export default function App() {
  const [result, setResult] = useState<number | undefined>();

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const valueToken: any = await setItemKS('token', 'new-token-value');
            setResult(valueToken);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Agregar item ejemplo "token" valor "new-token-value"
        </Text>
      </Pressable>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const valueToken: any = await getItemKS('token');
            setResult(valueToken);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Obtener item de ejemplo "token"
        </Text>
      </Pressable>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const valueTokenRemoved: any = await removeItemKS('token');
            setResult(valueTokenRemoved);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Remover item de ejemplo "token"
        </Text>
      </Pressable>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const clearResult: any = await clearAll();
            setResult(clearResult);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Elimina todo incluyendo KeyStorage y AsyncStorage
        </Text>
      </Pressable>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const storageNormal: any = await setItemStorage(
              'token-storage',
              'new-token-value-without-encryptation'
            );
            setResult(storageNormal);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Guardar en el storage sin encryptación
        </Text>
      </Pressable>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const storageNormal: any = await getItemStorage('token-storage');
            setResult(storageNormal);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Obtener en el storage sin encryptación
        </Text>
      </Pressable>
      <Pressable
        style={styles.pressButton}
        onPress={() => {
          (async () => {
            const storageNormal: any = await removeItemStorage('token-storage');
            setResult(storageNormal);
          })();
        }}
      >
        <Text style={styles.pressButtonFont}>
          Elimina en el storage sin encryptación
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  pressButton: {
    marginVertical: 10,
    paddingVertical: 15,
    backgroundColor: '#043494',
    paddingHorizontal: 12,
    borderRadius: 10,
  },
  pressButtonFont: {
    color: '#fff',
  },
});
