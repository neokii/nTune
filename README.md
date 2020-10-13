https://github.com/neokii/nTune/releases

1.1.8 부터 cameraOffset이 추가되어 앱(apk), ntune.py 둘다 업데이트해야 합니다.

<pre>
1. ntune.py를 이온의 /data/openpilot/selfdrive 위치로 복사합니다.


2. 각차량 interface에서 lqr 또는 indi가 지정되어야 동작합니다. 예) ret.lateralTuning.init('lqr')

    selfdrive/controls/lib/latcontrol_lqr.py
    selfdrive/controls/lib/latcontrol_indi.py

    의 파일을 아래와 같이 몇가지 추가합니다.

   1) 맨상단에 아래를 추가합니다.
    
    from selfdrive.ntune import nTune

   2) lqr:27라인, indi:46라인 근처 self.reset() 아래에
    
    self.reset()
    self.tune = nTune(CP, self) # 추가
    
   3) lqr:47라인, indi:66라인 근처 update 함수블럭 첫 줄에 추가합니다.
    
    def update(self, ..........):
        self.tune.check() # 추가

   4) steerRatio, steerActuatorDelay 추가

    pathplanner.py 의 상단
    from selfdrive.ntune import ntune_get

    102라인 근처  VM.update_params(...) 호출되는 바로 아래에
    VM.sR = ntune_get('steerRatio') # 추가

    118라인 근처 calc_states_after_delay 함수 호출부분 마지막 인자를
    CP.steerActuatorDelay -> ntune_get('steerActuatorDelay') 로 수정


   5) cameraOffset 추가

   selfdrive/controls/lib/lane_planner.py
   selfdrive/controls/controlsd.py

    두 파일의 상단에 import 추가
    from selfdrive.ntune import ntune_get

    버전마다 약간의 차이가 있을 수 있으니
    cameraOffset = ntune_get("cameraOffset") 를 추가하고
    CAMERA_OFFSET -> cameraOffset 으로 대체만 하시기 바랍니다.


    * controlsd.py 435라인 근처  CAMERA_OFFSET 을 아래와 같이 수정합니다.

    cameraOffset = ntune_get("cameraOffset")
    l_lane_close = left_lane_visible and (self.sm['pathPlan'].lPoly[3] < (1.08 - cameraOffset))
    r_lane_close = right_lane_visible and (self.sm['pathPlan'].rPoly[3] > -(1.08 + cameraOffset))

    * lane_planner.py 의 90라인 근처

    cameraOffset = ntune_get("cameraOffset")
    self.l_poly[3] += cameraOffset
    self.r_poly[3] += cameraOffset


3. 이온을 재부팅 후 최초한번 판다와 연결합니다.

4. release폴더에 있는 apk파일을 안드로이드 기기에 설치에 실행하여 사용합니다.

5. 버튼을 길게 누르면 기존 Step의 x10로 증가 또는 감소합니다.

6. 설정 파일은 이온의 /data/ntune 위치에 저장됩니다.

</pre>
