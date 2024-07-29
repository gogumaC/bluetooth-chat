package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

enum class BluetoothState {
    STATE_NONE,
    STATE_DISCOVERING,
    STATE_DISCOVERING_FINISHED,
    STATE_CONNECTING,
    STATE_CONNECTED,
    STATE_DISCONNECTED;
}