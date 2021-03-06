package com.ctrip.apollo;

public class Apollo {

  public static final String VERSION = "java-0.0.1-SNAPSHOT";

  private static Env m_env;

  public enum Env {
    LOCAL, DEV, FWS, FAT, UAT, LPT, PRO, TOOLS
  }

  public static void initialize(Env env) {
    m_env = env;
  }

  public static Env getEnv() {
    return m_env;
  }

}
