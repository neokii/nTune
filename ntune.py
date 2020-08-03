import os
import fcntl
import signal
import errno
import time
import json
import numpy as np

CONF_PATH = '/data/ntune/'
CONF_FILE = '/data/ntune/lat_lqr.json'

class nTune():
  def __init__(self, CP, lqr):
  
    self.invalidated = False
    
    self.CP = CP
    self.lqr = lqr
    
    self.lqr.A = np.array([0., 1., -0.22619643, 1.21822268]).reshape((2, 2))
    self.lqr.B = np.array([-1.92006585e-04, 3.95603032e-05]).reshape((2, 1))
    self.lqr.C = np.array([1., 0.]).reshape((1, 2))
    
    if not os.path.exists(CONF_PATH):
      os.makedirs(CONF_PATH)

    self.read()
    
    try:
      signal.signal(signal.SIGIO, self.handler)
      fd = os.open(CONF_PATH,  os.O_RDONLY)
      fcntl.fcntl(fd, fcntl.F_SETSIG, 0)
      fcntl.fcntl(fd, fcntl.F_NOTIFY, fcntl.DN_MODIFY | fcntl.DN_CREATE | fcntl.DN_MULTISHOT)
    except:
      pass
      
  def handler(self, signum, frame):
  
    try:
      if os.path.isfile(CONF_FILE):
        with open(CONF_FILE, 'r') as f:
          self.config = json.load(f)
          if self.checkValid():
            self.write_config(self.config)
    
    except:
      pass
    
    self.invalidated = True
    
  def check(self): # called by LatControlLQR.update
    if self.invalidated:
      self.invalidated = False
      self.update()


  def read(self):
    
    try:
      if os.path.isfile(CONF_FILE):
        with open(CONF_FILE, 'r') as f:
          self.config = json.load(f)
          if self.checkValid():
            self.write_config(self.config)
            
          self.update()
      else:
        self.write_default()
        
        with open(CONF_FILE, 'r') as f:
          self.config = json.load(f)
          if self.checkValid():
            self.write_config(self.config)
          self.update()
    
    except:
      return False
        
    return True
        
  def checkValue(self, key, min_, max_, default_):
    updated = False
    
    if key not in self.config:
      self.config.update({key:default_})
      updated = True
    elif min_ > self.config[key]:
      self.config.update({key:min_})
      updated = True
    elif max_ < self.config[key]:
      self.config.update({key:max_})
      updated = True
      
    return updated

  def checkValid(self):
    updated = False
  
    if self.checkValue("scale", 500.0, 5000.0, 2000.0):
      updated = True
      
    if self.checkValue("ki", 0.0, 0.2, 0.01):
      updated = True
    
    if self.checkValue("k_1", -150.0, -50.0, -100.0):
      updated = True
      
    if self.checkValue("k_2", 400.0, 500.0, 450.0):
      updated = True
    
    if self.checkValue("l_1", 0.1, 0.5, 0.22):
      updated = True
      
    if self.checkValue("l_2", 0.1, 0.5, 0.32):
      updated = True
    
    if self.checkValue("dcGain", 0.0020, 0.0040, 0.003):
      updated = True
      
    if self.checkValue("steerActuatorDelay", 0.1, 0.8, 0.3):
      updated = True

    if self.checkValue("steerLimitTimer", 0.6, 1.5, 0.8):
      updated = True

    if self.checkValue("steerMax", 1.0, 2.0, 1.3):
      updated = True

    return updated

  def update(self):

    self.lqr.scale = float(self.config["scale"])
    self.lqr.ki = float(self.config["ki"])

    self.lqr.K = np.array([float(self.config["k_1"]), float(self.config["k_2"])]).reshape((1, 2))
    self.lqr.L = np.array([float(self.config["l_1"]), float(self.config["l_2"])]).reshape((2, 1))

    self.lqr.dc_gain = float(self.config["dcGain"])

    self.CP.steerActuatorDelay = float(self.config["steerActuatorDelay"])
    self.lqr.sat_limit = float(self.config["steerLimitTimer"])

    self.CP.steerMaxBP = [0.0]
    self.CP.steerMaxV = [float(self.config["steerMax"])]

    self.lqr.x_hat = np.array([[0], [0]])
    self.lqr.reset()

  def read_cp(self):

    self.config = {}

    try:
      if self.CP is not None and self.CP.lateralTuning.which() == 'lqr':

        self.config["scale"] = round(self.CP.lateralTuning.lqr.scale, 2)
        self.config["ki"] = round(self.CP.lateralTuning.lqr.ki, 3)
        self.config["k_1"] = round(self.CP.lateralTuning.lqr.k[0], 1)
        self.config["k_2"] = round(self.CP.lateralTuning.lqr.k[1], 1)
        self.config["l_1"] = round(self.CP.lateralTuning.lqr.l[0], 3)
        self.config["l_2"] = round(self.CP.lateralTuning.lqr.l[1], 3)
        self.config["dcGain"] = round(self.CP.lateralTuning.lqr.dcGain, 5)

        self.config["steerActuatorDelay"] = round(self.CP.steerActuatorDelay, 2)
        self.config["steerLimitTimer"] = round(self.CP.steerLimitTimer, 2)
        self.config["steerMax"] = round(self.CP.steerMaxV[0], 2)
        
    except:
      pass
        
      
  def write_default(self):
  
    try:
      self.read_cp()
      self.checkValid()
      self.write_config(self.config)
    except:
      pass
      
  def write_config(self, conf):
    try:
      with open(CONF_FILE, 'w') as f:
        json.dump(conf, f, indent=2, sort_keys=False)
        os.chmod(CONF_FILE, 0o764)
    except IOError:
      
      try:
        if not os.path.exists(CONF_PATH):
          os.makedirs(CONF_PATH)
          
        with open(CONF_FILE, 'w') as f:
          json.dump(conf, f, indent=2, sort_keys=False)
          os.chmod(CONF_FILE, 0o764)
      except:
        pass
      
    