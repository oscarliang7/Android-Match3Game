package com.nativegame.match3game.game.effect.lightning;

import com.nativegame.match3game.game.layer.Layer;
import com.nativegame.nattyengine.engine.Engine;
import com.nativegame.nattyengine.entity.sprite.Sprite;
import com.nativegame.nattyengine.entity.sprite.modifier.FadeOutModifier;
import com.nativegame.nattyengine.entity.sprite.modifier.ScaleInModifier;
import com.nativegame.nattyengine.entity.sprite.modifier.ScaleOutModifier;
import com.nativegame.nattyengine.texture.Texture;
import com.nativegame.nattyengine.util.math.RandomUtils;

public class LightningGlitterEffect extends Sprite {

    private static final long TIME_TO_LIVE = 600;
    private static final long TIME_TO_FADE = 200;

    private final LightningGlitterEffectSystem mParent;
    private final ScaleInModifier mScaleInModifier;
    private final ScaleOutModifier mScaleOutModifier;
    private final FadeOutModifier mFadeOutModifier;
    private final float mRotationSpeed;

    private long mTotalTime;

    public LightningGlitterEffect(LightningGlitterEffectSystem lightningGlitterEffectSystem, Engine engine, Texture texture) {
        super(engine, texture);
        mParent = lightningGlitterEffectSystem;
        mScaleInModifier = new ScaleInModifier(TIME_TO_FADE);
        mScaleOutModifier = new ScaleOutModifier(TIME_TO_FADE, TIME_TO_LIVE - TIME_TO_FADE);
        mFadeOutModifier = new FadeOutModifier(TIME_TO_FADE, TIME_TO_LIVE - TIME_TO_FADE);
        mFadeOutModifier.setAutoRemove(true);
        mRotationSpeed = RandomUtils.nextSign() * 180f / 1000;
        setLayer(Layer.GRID_LAYER);   // We make sure not cover effect
    }

    //--------------------------------------------------------
    // Overriding methods
    //--------------------------------------------------------
    @Override
    public void onRemove() {
        mParent.returnToPool(this);
    }

    @Override
    public void onUpdate(long elapsedMillis) {
        mTotalTime += elapsedMillis;
        if (mTotalTime < TIME_TO_LIVE / 2) {
            mRotation += mRotationSpeed * elapsedMillis;
        } else {
            mRotation -= mRotationSpeed * elapsedMillis;
        }
        mScaleInModifier.update(this, elapsedMillis);
        mScaleOutModifier.update(this, elapsedMillis);
        mFadeOutModifier.update(this, elapsedMillis);
    }
    //========================================================

    //--------------------------------------------------------
    // Methods
    //--------------------------------------------------------
    public void activate(float x, float y) {
        setCenterX(x);
        setCenterY(y);
        setRotation(RandomUtils.nextFloat(360));
        mScaleInModifier.init(this);
        mFadeOutModifier.init(this);
        addToGame();
        mTotalTime = 0;
    }
    //========================================================

}