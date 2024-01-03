# react-native-protected-storage

Save sensitive data in storage protected

## Installation

```sh
npm install react-native-protected-storage
```

## Usage

```js
import {
  setItemKS,
  getItemKS,
  removeItemKS,
  clearAll,
} from 'react-native-protected-storage';

// ...

const result = await setItemKS(key, value);
const result = await getItemKS(key);
const result = await removeItemKS(key);
const result = await clearAll();
```

## Usage only storage without encryptation

```js
import {
  setItemStorage,
  getItemStorage,
  removeItemStorage,
} from 'react-native-protected-storage';

// ...

const result = await setItemStorage(key, value);
const result = await getItemStorage(key);
const result = await removeItemStorage(key);
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---
