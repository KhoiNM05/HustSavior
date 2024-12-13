package io.github.HustSavior.skills;

public class CooldownController {

    private static float cooldown;
    private float timer;
    public CooldownController(float defaultCooldown){
        this.cooldown=defaultCooldown;
        timer=0;
    }

    void cooldownTimer(float delta){
        timer+=delta;
    }

    boolean isReady(){
        return (timer>=cooldown);
    }

    void resetCooldown(){
        timer=0;
    }

    void setCooldown(float scale){
        cooldown*=scale;
    }
}
