package org.optframework.config;

import org.optframework.core.Log;

public class Config {
    public static final String configPath = "resources/config/config.yml";

    public static GlobalConfig global;

    public static SAConfig sa_algorithm;

    public static HoneyBeeConfig honeybee_algorithm;

    public static PACSAConfig pacsa_algorithm;

    public static PSOConfig pso_algorithm;


    public static void initConfig(){
        ReadConfig readConfig = ReadConfig.readYaml();

        Config.global = readConfig.global;
        Config.sa_algorithm = readConfig.sa_algorithm;
        Config.honeybee_algorithm = readConfig.honeybee_algorithm;
        Config.pacsa_algorithm = readConfig.pacsa_algorithm;
        Config.pso_algorithm = readConfig.pso_algorithm;
        Log.logger.info("config file is read");
    }
}
