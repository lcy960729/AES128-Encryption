# AES128-Encryption
AES128 암호화, 복호화 프로그램
암호화 라이브러리를 사용하지 않고 직접 구현

## 1. 플로우 차트
![플로우 차트](https://user-images.githubusercontent.com/58020519/106413712-255e4200-648e-11eb-82ea-376bd762e846.png)

## 2. 실행 화면
### 1) 초기 실행 화면
![초기 화면](https://user-images.githubusercontent.com/58020519/106417250-e5e82380-6496-11eb-99f2-3162fc11c91d.png)

### 2) 키 및 텍스트 입력
![텍스트 입력](https://user-images.githubusercontent.com/58020519/106417632-e7feb200-6497-11eb-918e-9518cd030bcd.png)

키와 텍스트는 입력 모드를 지정할 수 있다. Text모드와 Hex 모드를 지원하며 한글은 UTF-8인코딩을 하였다. 이후 Run 버튼 위에 있는 실행 모드(Encrypt or Decrypt)를 선택 후 Run 버튼을 눌러준다. 
### 3) 실행 결과 - 암호화
![암호화](https://user-images.githubusercontent.com/58020519/106417680-fa78eb80-6497-11eb-868b-0aeaa0b8c7c1.png)

실행결과는 암호문과 암호문의 hex값 그리고 각 라운드마다 상태 값 및 라운드 키를 확인 할 수 있다.
### 4) 실행 결과 - 복호화
![복호화](https://user-images.githubusercontent.com/58020519/106417713-0795da80-6498-11eb-904b-8eb22f09d5be.png)

3번에서의 암호문의 hex값을 input Text에 입력 후 Text input Type을 Hex 바꿔준다.
실행모드는 Decrypt를 선택 후 Run버튼을 누르면 복호화 된 텍스트가 출력된다.
## 3. 복호문 검증
암호화 할 때 각 라운드의 state값과 복호화 할 때 각 라운드의 state 값을 비교하면 서로 같은 state가 나오는 것을 확인 할 수 있다 
