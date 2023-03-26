package com.nativegame.match3game.game.algorithm.special.combine;

import com.nativegame.match3game.algorithm.TileState;
import com.nativegame.match3game.algorithm.TileType;
import com.nativegame.match3game.game.layer.tile.SpecialType;
import com.nativegame.match3game.game.layer.tile.Tile;
import com.nativegame.nattyengine.engine.Engine;
import com.nativegame.nattyengine.entity.timer.Timer;

import java.util.ArrayList;
import java.util.List;

public class ColorExplosionCombineHandler extends ColorCombineHandler implements Timer.TimerListener {

    private static final long START_DELAY = 1200;

    private final Timer mTimer;

    private final List<Tile> mSpecialTiles = new ArrayList<>();

    public ColorExplosionCombineHandler(Engine engine) {
        super(engine);
        mTimer = new Timer(engine, this, START_DELAY);
    }

    //--------------------------------------------------------
    // Overriding methods
    //--------------------------------------------------------
    @Override
    public long getStartDelay() {
        return START_DELAY;
    }

    @Override
    public boolean checkSpecialCombine(Tile[][] tiles, Tile tileA, Tile tileB, int row, int col) {
        if (tileA.getSpecialType() == SpecialType.COLOR_SPECIAL_TILE
                && tileB.getSpecialType() == SpecialType.EXPLOSION_SPECIAL_TILE) {
            handleSpecialCombine(tiles, tileA, tileB, row, col);
            return true;
        }
        if (tileB.getSpecialType() == SpecialType.COLOR_SPECIAL_TILE
                && tileA.getSpecialType() == SpecialType.EXPLOSION_SPECIAL_TILE) {
            handleSpecialCombine(tiles, tileB, tileA, row, col);
            return true;
        }

        return false;
    }

    @Override
    protected void playTileEffect(Tile colorTile, Tile fruitTile) {
        super.playTileEffect(colorTile, fruitTile);
        fruitTile.playTileEffect();
        colorTile.hideTile();
        fruitTile.hideTile();
        // Important to hide these tiles here, since the Algorithm hasn't been added yet
    }

    @Override
    public void onTimerComplete(Timer timer) {
        int size = mSpecialTiles.size();
        for (int i = 0; i < size; i++) {
            Tile tile = mSpecialTiles.get(i);
            tile.popTile();
        }
        mSpecialTiles.clear();
    }
    //========================================================

    //--------------------------------------------------------
    // Methods
    //--------------------------------------------------------
    private void handleSpecialCombine(Tile[][] tiles, Tile colorTile, Tile fruitTile, int row, int col) {
        colorTile.setTileState(TileState.MATCH);
        fruitTile.setTileState(TileState.MATCH);

        // Transform and pop the same type tile
        TileType targetType = fruitTile.getTileType();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                Tile t = tiles[i][j];
                // We make sure not pop multiple times
                if (t.getTileType() == targetType && t.getTileState() == TileState.IDLE) {
                    // We transform this tile to explosion special type
                    t.setSpecialType(SpecialType.EXPLOSION_SPECIAL_TILE);
                    mSpecialTiles.add(t);
                    // Add lightning effect from color tile to target tile
                    playLightningEffect(colorTile, t);
                }
            }
        }

        playTileEffect(colorTile, fruitTile);
        mTimer.startTimer();
    }
    //========================================================

}