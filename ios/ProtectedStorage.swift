import Security

@objc(ProtectedStorage)
class ProtectedStorage: NSObject {

  @objc(setItem:withValue:withResolver:withRejecter:)
  func setItem(name: String, value: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
    let key = name
    let password = value.data(using: .utf8)!
    let attributes: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrAccount as String: key,
      kSecValueData as String: password,
    ]

    let updateAttributes: [String: Any] = [
      kSecValueData as String: password,
    ]

    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrAccount as String: key,
    ]

    let status = SecItemUpdate(query as CFDictionary, updateAttributes as CFDictionary)

    if status == errSecSuccess {
      resolve("Item successfully updated.")
    } else if status == errSecItemNotFound {
      let result = SecItemAdd(attributes as CFDictionary, nil)
      if result == errSecSuccess {
        resolve("Item successfully added.")
      } else {
        resolve(nil)
      }
    } else {
      resolve(nil)
    }
  }

  @objc(getItem:withResolver:withRejecter:)
  func getItem(name: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    let key = name
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrAccount as String: key,
      kSecReturnData as String: true,
    ]

    var result: AnyObject?
    let status = SecItemCopyMatching(query as CFDictionary, &result)

    if status == errSecSuccess, let data = result as? Data {
      if let password = String(data: data, encoding: .utf8) {
        resolve(password)
      } else {
        reject("KeychainError", "Failed to decode data from keychain.", nil)
      }
    } else if status == errSecItemNotFound {
      resolve(nil)
    } else {
      reject("KeychainError", "Failed to retrieve item from keychain.", nil)
    }
  }

  @objc(removeItem:withResolver:withRejecter:)
  func removeItem(name: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    let key = name
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrAccount as String: key,
    ]

    let status = SecItemDelete(query as CFDictionary)

    if status == errSecSuccess {
      resolve("Item successfully removed.")
    } else if status == errSecItemNotFound {
      resolve(nil)
    } else {
      reject("KeychainError", "Failed to remove item from keychain.", nil)
    }
  }

  @objc(clearAll:withRejecter:)
  func clearAll(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
    ]

    let status = SecItemDelete(query as CFDictionary)

    if status == errSecSuccess || status == errSecItemNotFound {
      let defaults = UserDefaults.standard
      if let bundleID = Bundle.main.bundleIdentifier {
          defaults.removePersistentDomain(forName: bundleID)
      }
      resolve("All items successfully removed.")
    } else {
      resolve(nil)
    }
  }

  @objc(storageSetItem:withValue:withResolver:withRejecter:)
  func storageSetItem(name: String, value: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
    UserDefaults.standard.set(value, forKey: name)
    resolve("Save in storage")
  }

  @objc(storageGetItem:withResolver:withRejecter:)
  func storageGetItem(name: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    let userDefaults = UserDefaults.standard

    if let stringValue = userDefaults.string(forKey: name) {
        resolve(stringValue)
    } else {
        resolve(nil)
    }
  }

  @objc(storageRemoveItem:withResolver:withRejecter:)
  func storageRemoveItem(name: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
    UserDefaults.standard.removeObject(forKey: name)
    resolve(nil)
  }
}
