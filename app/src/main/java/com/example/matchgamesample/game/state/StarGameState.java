package com.example.matchgamesample.game.state;

import com.example.matchgamesample.engine.GameEngine;
import com.example.matchgamesample.engine.GameEvent;
import com.example.matchgamesample.game.Tile;
import com.example.matchgamesample.game.TileUtils;

import java.util.ArrayList;

public class StarGameState extends GameState {
    private final ArrayList<Integer> mTarget;

    public StarGameState(GameEngine gameEngine) {
        super(gameEngine);
        mTarget = gameEngine.mLevel.mCollect;
    }

    @Override
    public boolean isPlayerWin() {
        int size = mGameEngine.mLevel.mTarget.size();
        for(int i = 0; i < size; i++){
            if(mGameEngine.mLevel.mTarget.get(i) != 0){
                return false;
            }
        }
        mPlayerWin = true;
        return true;
    }

    @Override
    public void onUpdate(Tile[][] tileArray) {
        for (int j = 0; j < mColumn; j++) {
            for (int i = 0; i < mRow; i++) {

                Tile tile = tileArray[i][j];
                // Update starfish
                if (tile.kind == TileUtils.STAR_FISH && tile.entryPoint) {
                    tile.match++;
                    int target = mGameEngine.mLevel.mTarget.get(0);
                    if (target > 0) {
                        target--;
                        mGameEngine.mLevel.mTarget.set(0, target);
                        mGameEngine.onGameEvent(GameEvent.COLLECT);
                    }
                    continue;
                }

                if (tile.match == 0 || tile.layer != 0) {
                    continue;
                }

                // Update mCollect item
                int size = mTarget.size();
                for (int n = 1; n < size; n++) {
                    if (tile.kind == mTarget.get(n)) {
                        int target = mGameEngine.mLevel.mTarget.get(n);
                        if (target > 0) {
                            target--;
                            mGameEngine.mLevel.mTarget.set(n, target);
                            mGameEngine.onGameEvent(GameEvent.COLLECT);
                        }
                    }
                }
            }
        }
    }
}