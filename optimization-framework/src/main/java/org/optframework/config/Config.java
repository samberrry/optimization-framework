package org.optframework.config;

public class Config {
    public static final String configPath = "resources/config/config.yml";

    public static GlobalConfig global;

    public static SAConfig sa_algorithm;

    public static HoneyBeeConfig honeybee_algorithm;

    public static AntColonyConfig antcolony_algorithm;

    public static void initConfig(){
        ReadConfig readConfig = ReadConfig.readYaml();

        Config.global = readConfig.global;
        Config.sa_algorithm = readConfig.sa_algorithm;
        Config.honeybee_algorithm = readConfig.honeybee_algorithm;
        Config.antcolony_algorithm = readConfig.antcolony_algorithm;
    }
}
