

export function decode(b64: string) {
  return new Uint8Array(Buffer.from(b64, 'base64'))
}

export function encode(uint8array: Uint8Array) {
  return Buffer.from(uint8array).toString('base64');
}
