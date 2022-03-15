package com.example.matchgamesample.game.algorithm;

import com.example.matchgamesample.engine.GameEngine;
import com.example.matchgamesample.engine.GameEvent;
import com.example.matchgamesample.game.Tile;
import com.example.matchgamesample.game.TileUtils;

public class BonusTimeAlgorithm extends BaseAlgorithm {
    // Tile moving control
    public int mCurrentWaitingTime = 300;
    private int mWaitingTime = 0;
    private boolean mMoveTile = false;

    // Bonus time time control
    public int mCurrentBonusTimeInterval = 200;
    private int mBonusTime = 0;
    private boolean mLevelComplete = false;

    private BonusTimeState mState;

    private enum BonusTimeState {
        CLEAR_SPECIAL_FRUIT,
        BONUS_TIME,
        GAME_OVER
    }

    public BonusTimeAlgorithm(GameEngine gameEngine) {
        super(gameEngine);
        mState = BonusTimeState.CLEAR_SPECIAL_FRUIT;
    }

    @Override
    public void update(Tile[][] tileArray, long elapsedMillis) {

        // 1. Find match
        updateWait(tileArray);
        if (!waitFinding) {
            findMatch(tileArray);
        }

        // 2. Moving
        if (mMoveTile) {
            for (int i = 0; i < mRow; i++) {
                for (int j = 0; j < mColumn; j++) {
                    tileArray[i][j].onUpdate(elapsedMillis);
                    // Start bouncing animation
                    if (tileArray[i][j].bounce == 1) {
                        mAnimationManager.createLightBounceAnim(tileArray[i][j].mImage);
                    } else if (tileArray[i][j].bounce == 2) {
                        mAnimationManager.createHeavyBounceAnim(tileArray[i][j].mImage);
                    }
                }
            }
        }

        updateMove(tileArray);

        // 3. Fruit wait
        if (!isMoving && !waitFinding) {
            mMoveTile = false;
        } else {
            //Check is player swapping
            if (!mMoveTile) {
                mWaitingTime += elapsedMillis;
                if (mWaitingTime > mCurrentWaitingTime) {
                    mMoveTile = true;
                    mWaitingTime = 0;
                }
            }
        }

        updateMatch(tileArray);

        // Bonus time
        if (!isMoving && !waitFinding && !matchFinding) {

            if (mState == BonusTimeState.CLEAR_SPECIAL_FRUIT) {

                //Check is special fruit exist
                boolean isOver = true;
                outer:
                for (int i = 0; i < mRow; i++) {
                    for (int j = 0; j < mColumn; j++) {
                        //Explode special fruit
                        if (tileArray[i][j].special) {
                            tileArray[i][j].match++;
                            isOver = false;
                            break outer;
                        }
                    }
                }

                if (isOver) {
                    if (mLevelComplete) {
                        mState = BonusTimeState.GAME_OVER;
                    } else {
                        mState = BonusTimeState.BONUS_TIME;
                        mGameEngine.onGameEvent(GameEvent.BONUS_TIME);
                        mFruitNum = TileUtils.FRUITS.length;
                    }
                }

            } else if (mState == BonusTimeState.BONUS_TIME) {
                mBonusTime += elapsedMillis;
                if (mBonusTime > mCurrentBonusTimeInterval) {
                    if (mGameEngine.mLevel.mMove == 0) {
                        mState = BonusTimeState.CLEAR_SPECIAL_FRUIT;
                        mLevelComplete = true;
                    } else {
                        convertMove2SpecialTile(tileArray);
                    }
                    mBonusTime = 0;
                }
            } else if (mState == BonusTimeState.GAME_OVER) {
                mGameEngine.onGameEvent(GameEvent.BONUS_TIME_COMPLETE);
            }

        }

        // 5. Update tile
        if (!isMoving) {

            // (5.1) Check special fruit
            for (int j = 0; j < mColumn; j++) {
                for (int i = 0; i < mRow; i++) {
                    //Check is special fruit
                    if (tileArray[i][j].special
                            && tileArray[i][j].match != 0
                            && !tileArray[i][j].lock) {
                        // Check direction
                        if (tileArray[i][j].direct == 'H') {
                            //Check special combine
                            if (tileArray[i][j].specialCombine == 'R') {
                                explodeV(tileArray, tileArray[i][j]);
                            } else if (tileArray[i][j].specialCombine == 'G') {
                                explodeBigH(tileArray, tileArray[i][j]);
                            } else {
                                if (!tileArray[i][j].isExplode)
                                    explodeH(tileArray, tileArray[i][j]);
                            }
                        } else if (tileArray[i][j].direct == 'V') {
                            //Check special combine
                            if (tileArray[i][j].specialCombine == 'R') {
                                explodeH(tileArray, tileArray[i][j]);
                            } else if (tileArray[i][j].specialCombine == 'G') {
                                explodeBigV(tileArray, tileArray[i][j]);
                            } else {
                                if (!tileArray[i][j].isExplode)
                                    explodeV(tileArray, tileArray[i][j]);
                            }
                        } else if (tileArray[i][j].direct == 'S' && !tileArray[i][j].isExplode) {
                            //Check special combine
                            if (tileArray[i][j].specialCombine == 'B') {
                                explodeBigS(tileArray, tileArray[i][j]);
                            } else {
                                explodeS(tileArray, tileArray[i][j]);
                            }
                        } else if (tileArray[i][j].direct == 'I' && !tileArray[i][j].isExplode) {
                            //Check special combine
                            if (tileArray[i][j].specialCombine == 'T') {
                                transI(tileArray, tileArray[i][j]);
                            } else if (tileArray[i][j].specialCombine == 'S') {
                                transI(tileArray, tileArray[i][j]);
                            } else if (tileArray[i][j].specialCombine == 'M') {
                                explodeBigI(tileArray, tileArray[i][j]);
                            } else {
                                explodeI(tileArray, tileArray[i][j]);
                            }
                        }
                    }
                }
            }

            // (5.2) Add square special fruit
            for (int i = 0; i < mRow; i++) {
                for (int j = 1; j < mColumn - 1; j++) {
                    //Check state
                    if (tileArray[i][j].isFruit() && tileArray[i][j].wait == 0) {
                        //Check mRow for 3
                        if (tileArray[i][j].kind == tileArray[i][j - 1].kind &&
                                tileArray[i][j].kind == tileArray[i][j + 1].kind) {
                            //Check potential match
                            if (i > 0 && tileArray[i][j].kind == tileArray[i - 1][j - 1].kind
                                    && tileArray[i - 1][j - 1].match > 0) {            //Top left
                                //If tile is coco, do not add
                                if (!tileArray[i][j - 1].special) {
                                    if (i > 1 && tileArray[i - 2][j - 1].kind == tileArray[i][j].kind) {
                                        /* O
                                         * O
                                         * X O O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j - 1], 'L', 1);
                                        tileArray[i][j].isUpgrade = true;
                                        tileArray[i][j + 1].isUpgrade = true;
                                        tileArray[i - 1][j - 1].isUpgrade = true;
                                        tileArray[i - 2][j - 1].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j - 1].direct = 'S';
                                    } else if (i < mRow - 1 && tileArray[i + 1][j - 1].kind == tileArray[i][j].kind) {
                                        /* O
                                         * X O O
                                         * O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j - 1], 'L', 2);
                                        tileArray[i][j].isUpgrade = true;
                                        tileArray[i][j + 1].isUpgrade = true;
                                        tileArray[i - 1][j - 1].isUpgrade = true;
                                        tileArray[i + 1][j - 1].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j - 1].direct = 'S';
                                    }
                                }
                            } else if (i < mRow - 1 && tileArray[i][j].kind == tileArray[i + 1][j - 1].kind
                                    && tileArray[i + 1][j - 1].match > 0) {         //Bottom Left
                                //If tile is coco, do not add
                                if (!tileArray[i][j - 1].special) {
                                    if (i < mRow - 2 && tileArray[i + 2][j - 1].kind == tileArray[i][j].kind) {
                                        /* X O O
                                         * O
                                         * O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j - 1], 'L', 3);
                                        tileArray[i][j].isUpgrade = true;
                                        tileArray[i][j + 1].isUpgrade = true;
                                        tileArray[i + 1][j - 1].isUpgrade = true;
                                        tileArray[i + 2][j - 1].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j - 1].direct = 'S';
                                    }
                                }
                            } else if (i > 0 && tileArray[i][j].kind == tileArray[i - 1][j].kind && tileArray[i - 1][j].match > 0) {            //Top Center
                                //If tile is coco, do not add
                                if (!tileArray[i][j].special) {
                                    if (i > 1 && tileArray[i - 2][j].kind == tileArray[i][j].kind) {
                                        /*   O
                                         *   O
                                         * O X O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j], 'C', 1);
                                        tileArray[i][j - 1].isUpgrade = true;
                                        tileArray[i][j + 1].isUpgrade = true;
                                        tileArray[i - 1][j].isUpgrade = true;
                                        tileArray[i - 2][j].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j].direct = 'S';
                                    } else if (i < mRow - 1 && tileArray[i + 1][j].kind == tileArray[i][j].kind) {
                                        /*   O
                                         * O X O
                                         *   O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j], 'C', 2);
                                        tileArray[i][j - 1].isUpgrade = true;
                                        tileArray[i][j + 1].isUpgrade = true;
                                        tileArray[i - 1][j].isUpgrade = true;
                                        tileArray[i + 1][j].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j].direct = 'S';
                                    }
                                }
                            } else if (i < mRow - 1 && tileArray[i][j].kind == tileArray[i + 1][j].kind
                                    && tileArray[i + 1][j].match > 0) {              //Bottom Center
                                //If tile is coco, do not add
                                if (!tileArray[i][j].special) {
                                    if (i < mRow - 2 && tileArray[i + 2][j].kind == tileArray[i][j].kind) {
                                        /* O X O
                                         *   O
                                         *   O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j], 'C', 3);
                                        tileArray[i][j - 1].isUpgrade = true;
                                        tileArray[i][j + 1].isUpgrade = true;
                                        tileArray[i + 1][j].isUpgrade = true;
                                        tileArray[i + 2][j].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j].direct = 'S';
                                    }
                                }
                            } else if (i > 0 && tileArray[i][j].kind == tileArray[i - 1][j + 1].kind && tileArray[i - 1][j + 1].match > 0) {           //Top Right
                                //If tile is coco, do not add
                                if (!tileArray[i][j + 1].special) {
                                    if (i > 1 && tileArray[i - 2][j + 1].kind == tileArray[i][j].kind) {
                                        /*     O
                                         *     O
                                         * O O X
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j + 1], 'R', 1);
                                        tileArray[i][j - 1].isUpgrade = true;
                                        tileArray[i][j].isUpgrade = true;
                                        tileArray[i - 1][j + 1].isUpgrade = true;
                                        tileArray[i - 2][j + 1].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j + 1].direct = 'S';
                                    } else if (i < mRow - 1 && tileArray[i + 1][j + 1].kind == tileArray[i][j].kind) {
                                        /*     O
                                         * O O X
                                         *     O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j + 1], 'R', 2);
                                        tileArray[i][j - 1].isUpgrade = true;
                                        tileArray[i][j].isUpgrade = true;
                                        tileArray[i - 1][j + 1].isUpgrade = true;
                                        tileArray[i + 1][j + 1].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j + 1].direct = 'S';
                                    }
                                }
                            } else if (i < mRow - 1 && tileArray[i][j].kind == tileArray[i + 1][j + 1].kind && tileArray[i + 1][j + 1].match > 0) {                 //Bottom Right
                                //If tile is coco, do not add
                                if (!tileArray[i][j + 1].special) {
                                    if (i < mRow - 2 && tileArray[i + 2][j + 1].kind == tileArray[i][j].kind) {
                                        /* O O X
                                         *     O
                                         *     O
                                         */
                                        //Add upgrade animation
                                        mAnimationManager.upgrade2S(tileArray[i][j + 1], 'R', 3);
                                        tileArray[i][j - 1].isUpgrade = true;
                                        tileArray[i][j].isUpgrade = true;
                                        tileArray[i + 1][j + 1].isUpgrade = true;
                                        tileArray[i + 2][j + 1].isUpgrade = true;
                                        //Make it special
                                        tileArray[i][j + 1].direct = 'S';
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // (5.3) Add vertical special fruit
            for (int i = 0; i < mRow; i++) {
                for (int j = 0; j < mColumn - 3; j++) {
                    //Check state
                    if (tileArray[i][j].isFruit() && tileArray[i][j].wait == 0) {
                        // Check mRow for >= 4
                        if (tileArray[i][j].match > 0 && tileArray[i][j + 1].match > 0
                                && tileArray[i][j].kind == tileArray[i][j + 1].kind
                                && tileArray[i][j].kind == tileArray[i][j + 2].kind
                                && tileArray[i][j].kind == tileArray[i][j + 3].kind) {
                            //Check mRow for 5
                            if (j < mColumn - 4 && tileArray[i][j].kind == tileArray[i][j + 4].kind) {
                                //If tile is already special, do not add
                                if (tileArray[i][j + 2].direct == 'N' && !tileArray[i][j + 2].isUpgrade) {
                                    //Add upgrade animation
                                    mAnimationManager.upgrade2I_h(tileArray[i][j + 2]);
                                    tileArray[i][j].isUpgrade = true;
                                    tileArray[i][j + 1].isUpgrade = true;
                                    tileArray[i][j + 3].isUpgrade = true;
                                    tileArray[i][j + 4].isUpgrade = true;
                                    //Make it special
                                    tileArray[i][j + 2].direct = 'I';
                                    tileArray[i][j + 2].kind = TileUtils.ICE_CREAM;
                                }
                            } else {
                                //If tile is already special, do not add
                                if (tileArray[i][j + 1].direct == 'N' && !tileArray[i][j + 1].isUpgrade) {
                                    //Add upgrade animation
                                    mAnimationManager.upgrade2H_left(tileArray[i][j + 1]);
                                    tileArray[i][j].isUpgrade = true;
                                    tileArray[i][j + 2].isUpgrade = true;
                                    tileArray[i][j + 3].isUpgrade = true;
                                    //Make it special
                                    tileArray[i][j + 1].direct = 'V';
                                }
                            }
                        }
                    }
                }
            }

            // (5.4) Add horizontal special fruit
            for (int j = 0; j < mColumn; j++) {
                for (int i = 0; i < mRow - 3; i++) {
                    //Check state
                    if (tileArray[i][j].isFruit() && tileArray[i][j].wait == 0) {
                        //Check mColumn for >= 4
                        if (tileArray[i][j].match > 0 && tileArray[i + 1][j].match > 0
                                && tileArray[i][j].kind == tileArray[i + 1][j].kind
                                && tileArray[i][j].kind == tileArray[i + 2][j].kind
                                && tileArray[i][j].kind == tileArray[i + 3][j].kind) {
                            //Check mRow for 5
                            if (i < mRow - 4 && tileArray[i][j].kind == tileArray[i + 4][j].kind) {
                                //If tile is already special, do not add
                                if (tileArray[i + 2][j].direct == 'N' && !tileArray[i + 2][j].isUpgrade) {
                                    //Add upgrade animation
                                    mAnimationManager.upgrade2I_v(tileArray[i + 2][j]);
                                    tileArray[i][j].isUpgrade = true;
                                    tileArray[i + 1][j].isUpgrade = true;
                                    tileArray[i + 3][j].isUpgrade = true;
                                    tileArray[i + 4][j].isUpgrade = true;
                                    //Make it special
                                    tileArray[i + 2][j].direct = 'I';
                                    tileArray[i + 2][j].kind = TileUtils.ICE_CREAM;
                                }
                            } else {
                                //If tile is already special, do not add
                                if (tileArray[i + 1][j].direct == 'N' && !tileArray[i + 1][j].isUpgrade) {
                                    //Add upgrade animation
                                    mAnimationManager.upgrade2V_top(tileArray[i + 1][j]);
                                    tileArray[i][j].isUpgrade = true;
                                    tileArray[i + 2][j].isUpgrade = true;
                                    tileArray[i + 3][j].isUpgrade = true;
                                    //Make it special
                                    tileArray[i + 1][j].direct = 'H';
                                }
                            }
                        }
                    }
                }
            }

            // (5.5) Check invalid tile
            for (int i = mRow - 1; i >= 0; i--) {
                for (int j = 0; j < mColumn; j++) {

                    // Check invalid tile match
                    if (tileArray[i][j].invalid && tileArray[i][j].match != 0) {

                        if (i == 0) {
                            // Add match to whole mColumn if not waiting
                            for (int m = i + 1; m < mRow; m++) {
                                // Check invalid obstacle (if match != 0 means can go down)
                                if (tileArray[m][j].tube) {
                                    continue;
                                } else if (tileArray[m][j].invalid && tileArray[m][j].match == 0) {
                                    break;
                                } else if (tileArray[m][j].wait != 0) {
                                    tileArray[m][j].wait = 0;
                                    tileArray[m][j].match++;
                                }
                            }
                        } else {
                            // Check up 3 in mRow, if any mColumn tile is available, then can falling down
                            for (int n = j - 1; n <= j + 1; n++) {

                                if (n < 0 || n >= mColumn)
                                    continue;

                                // If find tile can fill
                                if (!tileArray[i - 1][n].invalid || (tileArray[i - 1][n].tube && n == j)) {

                                    /* The tile can only go though tube vertically from bottom
                                     *      | |   <-- tube
                                     *      | |
                                     *     x o x  <-- tile (No diagonal swapping)
                                     */

                                    // Add match to whole mColumn if not waiting
                                    for (int m = i + 1; m < mRow; m++) {
                                        // Check invalid obstacle (if match != 0 means can go down)
                                        if (tileArray[m][j].tube) {
                                            continue;
                                        } else if (tileArray[m][j].invalid && tileArray[m][j].match == 0) {
                                            break;
                                        } else if (tileArray[m][j].wait != 0) {
                                            tileArray[m][j].wait = 0;
                                            tileArray[m][j].match++;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // (5.7) Add score
            for (int j = 0; j < mColumn; j++) {
                for (int i = 0; i < mRow; i++) {
                    if (tileArray[i][j].match != 0 && tileArray[i][j].isFruit()) {
                        mGameEngine.onGameEvent(GameEvent.PLAYER_SCORE);
                    }
                }
            }

            // (5.8) Add animation
            for (int j = 0; j < mColumn; j++) {
                for (int i = 0; i < mRow; i++) {

                    //Check is match
                    if (!tileArray[i][j].empty
                            && tileArray[i][j].match != 0
                            && tileArray[i][j].kind != 0
                            && !tileArray[i][j].isAnimate) {

                        // Set isAnimate
                        tileArray[i][j].isAnimate = true;

                        // Check is starfish
                        if (tileArray[i][j].kind == TileUtils.STAR_FISH) {
                            if (tileArray[i][j].entryPoint) {
                                mAnimationManager.explodeStarFish(tileArray[i][j]);
                            } else {
                                tileArray[i][j].match = 0;
                            }
                            continue;
                        }

                        if (tileArray[i][j].direct != 'N' && !tileArray[i][j].special) {
                            tileArray[i][j].special = true;
                            tileArray[i][j].match = 0;
                            continue;
                        }

                        // Explode fruit
                        if (!tileArray[i][j].isUpgrade)
                            mAnimationManager.explodeFruit(tileArray[i][j]);
                        // Show score
                        mAnimationManager.createScore(tileArray[i][j]);
                    }
                }
            }

            // (5.9) Reset
            tile2Top(tileArray);
            tileReset(tileArray);
        }

        // 6. Diagonal swapping
        updateWait(tileArray);
        diagonalSwap(tileArray);

        if (waitFinding) {
            tile2Top(tileArray);
            tileReset(tileArray);
        }
    }

    private void convertMove2SpecialTile(Tile[][] tileArray) {
        int random_row;
        int random_column;
        do {
            random_row = (int) (Math.random() * mRow);
            random_column = (int) (Math.random() * mColumn);
        } while (tileArray[random_row][random_column].special
                || !tileArray[random_row][random_column].isFruit());

        //Make it special
        tileArray[random_row][random_column].special = true;
        int random_direction = (int) (Math.random() * 3);

        switch (random_direction) {
            case 0:
                tileArray[random_row][random_column].direct = 'H';
                break;
            case 1:
                tileArray[random_row][random_column].direct = 'V';
                break;
            case 2:
                tileArray[random_row][random_column].direct = 'S';
                break;
        }

        //Add animation
        mAnimationManager.createTransformAnim(tileArray[random_row][random_column]);

        // Update swap
        mGameEngine.onGameEvent(GameEvent.PLAYER_SWAP);

    }

}
