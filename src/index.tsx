import VrcOsc from './NativeVrcOsc';

export function createClient(address: string, port: number): void {
  VrcOsc.createClient(address, port);
}

export function createServer(address: string, port: number): void {
  VrcOsc.createServer(address, port);
}

export function sendMessage(address: string, data: any[]): void {
  VrcOsc.sendMessage(address, data);
}
