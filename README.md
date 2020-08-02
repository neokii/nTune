
1. ntune.py 를 /data/openpilot/selfdrive 위치로 복사합니다.


2. /data/openpilot/selfdrive/controls/lib/latcontrol_lqr.py 파일을 수정합니다.

    1) 맨상단에 아래를 추가합니다.
    
    <pre>
    <code>
    from selfdrive.ntune import nTune
    
    </code>
    </pre>
    
    2) 27라인 근처 self.reset() 아래에
    
    <pre>
    <code>
    self.reset()
    self.tune = nTune(CP, self) # 추가
    
    </code>
    </pre>
    
    3) 48라인 근처 update 함수블럭 첫 줄에 추가
    
    <pre>
    <code>
    def update(self, active, CS, CP, path_plan):
        self.tune.check() # 추가
    
    </code>
    </pre>
            
    

3. 이온을 최초한번 판다와 연결합니다.


4. apk파일을 안드로이드 기기에 설치에 실행하여 사용합니다.
