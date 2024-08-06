![image](https://github.com/user-attachments/assets/e44a783e-e25a-48dd-95af-f5ca449b230f)

## Overview

![image](https://github.com/user-attachments/assets/2794dd75-8142-4665-9138-11461ea69c33)

- ![Android Studio](https://img.shields.io/badge/android%20studio-346ac1?style=for-the-badge&logo=android%20studio&logoColor=white) ![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) <img src="https://img.shields.io/badge/Bluetooth-3776AB?style=for-the-badge&logo=Bluetooth&logoColor=white">
- <img src="https://img.shields.io/badge/Compose-346ac1?style=for-the-badge&logo=Compose&logoColor=white"> <img src="https://img.shields.io/badge/Coroutine-346ac1?style=for-the-badge&logo=Coroutine&logoColor=white">

- 블루투스를 통해 주변의 안드로이드 기기와 채팅을 주고받을 수 있습니다.
- 안드로이드 코드랩의 [예제](https://github.com/android/connectivity-samples/tree/main/BluetoothChat)를 참고하여 최근 코드스타일로 수정하였습니다.
  - Java -> Kotlin
  - XML -> Compose
  - Thread -> Coroutine, Flow
- 진행 기간 : 24.07.20 ~ 24.08.06

## How to use?

1. 두 개의 기기에 BluetoothChat다운로드
2. (선택) 기기 스캔하기
   기기1|기기2
   ---|---
   ![image](https://github.com/user-attachments/assets/a94efdf6-7478-462e-bd77-8975b49b6ad2)| ![image](https://github.com/user-attachments/assets/06d80408-21fc-4198-86f8-e23db37aa17c)
   기기1 에서 '검색가능하게 설정' 버튼 클릭 후 '허용' 클릭 | 기기2에서 '+새 기기 연결하기' 클릭 후 나타난 다이얼로그에서 페어링을 원하는 기기를 클릭


3. 연결하기

   기기1|기기2
   ---|---
   ![image](https://github.com/user-attachments/assets/02387a76-f7ae-43c3-aaff-ed1f232c823c) | ![image](https://github.com/user-attachments/assets/ebf19b16-ada7-40f9-8cc1-e7b28d069757)
   기기1 에서 '서버소켓 열기' 클릭 후 연결이 요청될 때까지 대기 | 기기2의 저장된 기기 목록에서 연결을 원하는 기기를 확인 한 후 왼쪽으로 스와이프하여 연결요청

4. 채팅
   
   ![image](https://github.com/user-attachments/assets/0fa631b0-0180-4715-be98-c5ab62255844)
   - 원하는 내용 입력후 오른쪽 보내기 버튼을 통해 전송 (최대 1024byte전달 가능)

5. 연결 종료
   
   ![image](https://github.com/user-attachments/assets/643f1730-9b23-4e87-ae62-4ac549b10c1e)
   - 뒤로가기 버튼 클릭 후 나타난 다이얼로그에서 확인 선택



