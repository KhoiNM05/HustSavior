package io.github.HustSavior.spawner;

import com.badlogic.gdx.utils.Pool;

import io.github.HustSavior.entities.AbstractMonster;
import io.github.HustSavior.entities.FlyingEye;
import io.github.HustSavior.entities.Goblin;
import io.github.HustSavior.entities.Mushroom;
import io.github.HustSavior.entities.Skeleton;

public class MonsterPool {
    private final Pool<Skeleton> skeletonPool;
    private final Pool<Mushroom> mushroomPool;
    private final Pool<Goblin> goblinPool;
    private final Pool<FlyingEye> flyingEyePool;

    public MonsterPool() {
        skeletonPool = new Pool<Skeleton>(50) {
            @Override
            protected Skeleton newObject() {
                return new Skeleton(0, 0);
            }
        };
        
        mushroomPool = new Pool<Mushroom>(30) {
            @Override
            protected Mushroom newObject() {
                return new Mushroom(0, 0);
            }
        };
        
        goblinPool = new Pool<Goblin>(35) {
            @Override
            protected Goblin newObject() {
                return new Goblin(0, 0);
            }
        };
        
        flyingEyePool = new Pool<FlyingEye>(40) {
            @Override
            protected FlyingEye newObject() {
                return new FlyingEye(0, 0);
            }
        };
    }

    public AbstractMonster spawnMonster(String type, float x, float y) {
        AbstractMonster monster;
        switch (type) {
            case "FlyingEye":
                monster = new FlyingEye(x, y);
                break;
            case "Goblin":
                monster = new Goblin(x, y);
                break;
            case "Mushroom":
                monster = new Mushroom(x, y);
                break;
            case "Skeleton":
                monster = new Skeleton(x, y);
                break;
            default:
                return null;
        }
        return monster;
    }

    public void free(AbstractMonster monster) {
        if (monster instanceof Skeleton) {
            skeletonPool.free((Skeleton) monster);
        } else if (monster instanceof Mushroom) {
            mushroomPool.free((Mushroom) monster);
        } else if (monster instanceof Goblin) {
            goblinPool.free((Goblin) monster);
        } else if (monster instanceof FlyingEye) {
            flyingEyePool.free((FlyingEye) monster);
        }
    }

    public void resetMonster(AbstractMonster monster, float x, float y) {
        monster.setPosition(x, y);
        monster.setVelocity(0, 0);
    }

    public AbstractMonster obtain(int monsterType, float x, float y) {
        switch (monsterType) {
            case 0: return new Skeleton(x, y);
            case 1: return new FlyingEye(x, y);
            case 2: return new Mushroom(x, y);
            case 3: return new Goblin(x, y);
            default: return new Skeleton(x, y);
        }
    }
} 