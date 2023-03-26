package com.nativegame.match3game.game.layer.tile.type;

import com.nativegame.match3game.algorithm.TileState;
import com.nativegame.match3game.asset.Sounds;
import com.nativegame.match3game.asset.Textures;
import com.nativegame.match3game.game.effect.piece.StarfishPieceEffect;
import com.nativegame.match3game.game.layer.tile.FruitType;
import com.nativegame.match3game.game.layer.tile.TileSystem;
import com.nativegame.nattyengine.engine.Engine;
import com.nativegame.nattyengine.texture.Texture;

public class StarfishTile extends SolidTile {

    private final StarfishPieceEffect mStarfishPieceEffect;

    private boolean mIsStarfish = true;

    public StarfishTile(TileSystem tileSystem, Engine engine, Texture texture) {
        super(tileSystem, engine, texture, FruitType.NONE);
        mStarfishPieceEffect = new StarfishPieceEffect(engine, Textures.STARFISH);
    }

    //--------------------------------------------------------
    // Getter and Setter
    //--------------------------------------------------------
    public boolean isStarfish() {
        return mIsStarfish;
    }
    //========================================================

    //--------------------------------------------------------
    // Overriding methods
    //--------------------------------------------------------
    @Override
    public void popTile() {
        if (mIsStarfish) {
            return;
        }
        super.popTile();
    }

    @Override
    public void playTileEffect() {
        if (mIsStarfish) {
            playStarfishEffect();
            mIsStarfish = false;
            return;
        }
        super.playTileEffect();
    }

    @Override
    public boolean isShufflable() {
        if (mIsStarfish) {
            return false;
        }
        return super.isShufflable();
    }
    //========================================================

    //--------------------------------------------------------
    // Methods
    //--------------------------------------------------------
    public void popStarfishTile() {
        // Important to not reuse popTile() or matchTile()
        if (!mIsStarfish) {
            return;
        }
        mTileState = TileState.MATCH;
    }

    public void playStarfishEffect() {
        mStarfishPieceEffect.activate(getCenterX(), getCenterY());
        Sounds.COLLECT_STARFISH.play();
    }
    //========================================================

}