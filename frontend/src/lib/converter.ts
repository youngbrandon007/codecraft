import {fromUint8Array, toUint8Array} from "js-base64";

export function decode(b64: string) {
  return toUint8Array(b64)
}

export function encode(uint8array: Uint8Array) {
  return fromUint8Array(uint8array)
}
