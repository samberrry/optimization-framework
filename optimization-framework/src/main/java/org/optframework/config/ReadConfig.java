package org.optframework.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadConfig {
    public static final String configPath = "resources/config/config.yml";

    GlobalConfig global;

    SAConfig sa_algorithm;

    HoneyBeeConfig honeybee_algorithm;

    PACSAConfig pacsa_algorithm;

    PSOConfig pso_algorithm;

    public static ReadConfig readYaml(){
        Yaml yaml = new Yaml();

        InputStream in = null;
        try {
            in = Files.newInputStream( Paths.get(ReadConfig.configPath) );
        } catch (IOException e) {
            e.printStackTrace();
        }

        ReadConfig config = null;
        try {
            config = yaml.loadAs( in, ReadConfig.class );
        } catch (Exception e) {
            System.out.println("Bad config file! please check the config file...");
        }
        return config;
    }


    public GlobalConfig getGlobal() {
        return global;
    }

    public void setGlobal(GlobalConfig global) {
        this.global = global;
    }

    public SAConfig getSa_algorithm() {
        return sa_algorithm;
    }

    public void setSa_algorithm(SAConfig sa_algorithm) {
        this.sa_algorithm = sa_algorithm;
    }

    public HoneyBeeConfig getHoneybee_algorithm() {
        return honeybee_algorithm;
    }

    public void setHoneybee_algorithm(HoneyBeeConfig honeybee_algorithm) {
        this.honeybee_algorithm = honeybee_algorithm;
    }

    public PACSAConfig getPacsa_algorithm() {
        return pacsa_algorithm;
    }

    public void setPacsa_algorithm(PACSAConfig pacsa_algorithm) {
        this.pacsa_algorithm = pacsa_algorithm;
    }

    public PSOConfig getPso_algorithm() {
        return pso_algorithm;
    }

    public void setPso_algorithm(PSOConfig pso_algorithm) {
        this.pso_algorithm = pso_algorithm;
    }
}
