import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-protected-storage' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ProtectedStorage = NativeModules.ProtectedStorage
  ? NativeModules.ProtectedStorage
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function setItemKS(name: string, value: string): Promise<string> {
  return ProtectedStorage.setItem(name, value);
}

export function getItemKS(name: string): Promise<string> {
  return ProtectedStorage.getItem(name);
}

export function removeItemKS(name: string): Promise<string> {
  return ProtectedStorage.removeItem(name);
}

export function clearAll(): Promise<string> {
  return ProtectedStorage.clearAll();
}

export function setItemStorage(name: string, value: string): Promise<string> {
  return ProtectedStorage.storageSetItem(name, value);
}

export function getItemStorage(name: string): Promise<string> {
  return ProtectedStorage.storageGetItem(name);
}

export function removeItemStorage(name: string): Promise<string> {
  return ProtectedStorage.storageRemoveItem(name);
}
