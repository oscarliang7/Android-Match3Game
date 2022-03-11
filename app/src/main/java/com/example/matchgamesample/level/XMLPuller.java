package com.example.matchgamesample.level;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

public class XMLPuller {
    private static final String FILE_NAME = "data.xml";
    private final Context context;
    private Level mLevel;
    private String currentLevel, nextLevel;

    public XMLPuller(Context context) {
        this.context = context;

    }

    public Level getLevel(int level) {
        this.currentLevel = "level" + level;
        this.nextLevel = "level" + (level + 1);
        parseXML();
        return this.mLevel;
    }

    private void parseXML() {
        XmlPullParserFactory parserFactory;
        InputStream file = null;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            file = context.getAssets().open(FILE_NAME);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(file, null);
            processParsing(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = null;
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.getName();

                if (currentLevel.equals(tagName)) {
                    // Initialize mLevel when finding currentLevel
                    this.mLevel = new Level();
                } else if (nextLevel.equals(tagName)) {
                    // Break loop when finish assign mLevel data
                    break;
                } else if (this.mLevel != null) {
                    // Assign mLevel data when found
                    switch (tagName) {
                        case ("target_type"):
                            mLevel.setLevelType(Integer.parseInt(parser.nextText()));
                            break;
                        case ("move"):
                            mLevel.mMove = Integer.parseInt(parser.nextText());
                            break;
                        case ("fruit_num"):
                            mLevel.mFruitNum = Integer.parseInt(parser.nextText());
                            break;
                        case ("column"):
                            mLevel.mColumn = Integer.parseInt(parser.nextText());
                            break;
                        case ("row"):
                            mLevel.mRow = Integer.parseInt(parser.nextText());
                            break;
                        case ("target"):
                            mLevel.addTarget(Integer.parseInt(parser.nextText()));
                            break;
                        case ("collect"):
                            mLevel.addCollect(parser.nextText());
                            break;
                        case ("board"):
                            mLevel.board = parser.nextText();
                            break;
                        case ("fruit"):
                            mLevel.fruit = parser.nextText();
                            break;
                        case ("ice"):
                            mLevel.ice = parser.nextText();
                            break;
                        case ("ad"):
                            mLevel.advance = parser.nextText();
                            break;
                    }
                }
            }

            eventType = parser.next();
        }
    }

}
