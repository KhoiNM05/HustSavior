package io.github.HustSavior.entities;

public interface MonsterBehavior {
    void update(float delta, Player player);
    void takeDamage(float damage);
    boolean isAlive();
    float getSpeed();
 //   void setState(NormalMonster.MonsterState state);
//    NormalMonster.MonsterState getState();
}
