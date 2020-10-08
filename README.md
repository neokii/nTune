https://github.com/neokii/nTune/releases


1. ntune.py를 이온의 /data/openpilot/selfdrive 위치로 복사합니다.


2. 각차량 interface에서 lqr 또는 indi가 지정되어야 동작합니다. 예) ret.lateralTuning.init('lqr')

    **selfdrive/controls/lib/pathplanner.py**<br/>
    **selfdrive/controls/lib/latcontrol_lqr.py**<br/>
    **selfdrive/controls/lib/latcontrol_indi.py**<br/>

    의 파일을 아래와 같이 몇가지 추가합니다.

    1) 맨상단에 아래를 추가합니다.
    
    **from selfdrive.ntune import nTune**
    
    2) lqr:27라인, indi:46라인 근처 self.reset() 아래에
    
    self.reset()<br/>
    **self.tune = nTune(CP, self)** # 추가
    
    3) lqr:47라인, indi:66라인 근처 update 함수블럭 첫 줄에 추가합니다.
    
    def update(self, ..........):<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;**self.tune.check()** # 추가

    4) steerRatio, steerActuatorDelay 추가

    pathplanner.py 의 68라인 근처

    ...<br/>
    self.prev_torque_applied = False<br/>
    아래

    **self.tune = nTune(CP)** # 추가

     102라인 근처  VM.update_params(...) 호출되는 바로 아래에<br/>
    **VM.sr = self.tune.get('steerRatio')** # 추가

    118라인 근처 calc_states_after_delay 함수 호출부분 마지막 인자를<br/>
    CP.steerActuatorDelay -> **self.tune.get('steerActuatorDelay')** 로 수정


3. 이온을 재부팅 후 최초한번 판다와 연결합니다.

4. release폴더에 있는 apk파일을 안드로이드 기기에 설치에 실행하여 사용합니다.

5. 버튼을 길게 누르면 기존 Step의 x10로 증가 또는 감소합니다.

6. 설정 파일은 이온의 /data/ntune 위치에 저장됩니다.
