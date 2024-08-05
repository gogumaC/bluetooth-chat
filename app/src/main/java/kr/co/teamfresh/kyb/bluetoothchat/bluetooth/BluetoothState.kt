package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

enum class BluetoothState {
    STATE_DISABLE,
    STATE_NONE,
    STATE_DISCOVERING,
    STATE_DISCOVERING_FINISHED,
    STATE_START_BOND,
    STATE_BONDING,
    STATE_BONDED,
    STATE_BONDING_REJECTED,
    STATE_OPEN_SERVER_SOCKET,
    STATE_CONNECTING,
    STATE_CONNECTED,
    STATE_DISCONNECTED;
}