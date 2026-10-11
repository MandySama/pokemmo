package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Event;

import static org.lwjgl.glfw.GLFW.*;

final class GlfwKeyMap {
    private GlfwKeyMap() {
    }

    static int translate(int key) {
        return switch (key) {
            case GLFW_KEY_ESCAPE -> Event.KEY_ESCAPE;
            case GLFW_KEY_1 -> Event.KEY_1;
            case GLFW_KEY_2 -> Event.KEY_2;
            case GLFW_KEY_3 -> Event.KEY_3;
            case GLFW_KEY_4 -> Event.KEY_4;
            case GLFW_KEY_5 -> Event.KEY_5;
            case GLFW_KEY_6 -> Event.KEY_6;
            case GLFW_KEY_7 -> Event.KEY_7;
            case GLFW_KEY_8 -> Event.KEY_8;
            case GLFW_KEY_9 -> Event.KEY_9;
            case GLFW_KEY_0 -> Event.KEY_0;
            case GLFW_KEY_BACKSPACE -> Event.KEY_BACK;
            case GLFW_KEY_TAB -> Event.KEY_TAB;
            case GLFW_KEY_Q -> Event.KEY_Q;
            case GLFW_KEY_W -> Event.KEY_W;
            case GLFW_KEY_E -> Event.KEY_E;
            case GLFW_KEY_R -> Event.KEY_R;
            case GLFW_KEY_T -> Event.KEY_T;
            case GLFW_KEY_Y -> Event.KEY_Y;
            case GLFW_KEY_U -> Event.KEY_U;
            case GLFW_KEY_I -> Event.KEY_I;
            case GLFW_KEY_O -> Event.KEY_O;
            case GLFW_KEY_P -> Event.KEY_P;
            case GLFW_KEY_ENTER -> Event.KEY_RETURN;
            case GLFW_KEY_LEFT_CONTROL -> Event.KEY_LCONTROL;
            case GLFW_KEY_A -> Event.KEY_A;
            case GLFW_KEY_S -> Event.KEY_S;
            case GLFW_KEY_D -> Event.KEY_D;
            case GLFW_KEY_F -> Event.KEY_F;
            case GLFW_KEY_G -> Event.KEY_G;
            case GLFW_KEY_H -> Event.KEY_H;
            case GLFW_KEY_J -> Event.KEY_J;
            case GLFW_KEY_K -> Event.KEY_K;
            case GLFW_KEY_L -> Event.KEY_L;
            case GLFW_KEY_LEFT_SHIFT -> Event.KEY_LSHIFT;
            case GLFW_KEY_Z -> Event.KEY_Z;
            case GLFW_KEY_X -> Event.KEY_X;
            case GLFW_KEY_C -> Event.KEY_C;
            case GLFW_KEY_V -> Event.KEY_V;
            case GLFW_KEY_B -> Event.KEY_B;
            case GLFW_KEY_N -> Event.KEY_N;
            case GLFW_KEY_M -> Event.KEY_M;
            case GLFW_KEY_RIGHT_SHIFT -> Event.KEY_RSHIFT;
            case GLFW_KEY_LEFT_ALT -> Event.KEY_LMENU;
            case GLFW_KEY_SPACE -> Event.KEY_SPACE;
            case GLFW_KEY_CAPS_LOCK -> Event.KEY_CAPITAL;
            case GLFW_KEY_F1 -> Event.KEY_F1;
            case GLFW_KEY_F2 -> Event.KEY_F2;
            case GLFW_KEY_F3 -> Event.KEY_F3;
            case GLFW_KEY_F4 -> Event.KEY_F4;
            case GLFW_KEY_F5 -> Event.KEY_F5;
            case GLFW_KEY_F6 -> Event.KEY_F6;
            case GLFW_KEY_F7 -> Event.KEY_F7;
            case GLFW_KEY_F8 -> Event.KEY_F8;
            case GLFW_KEY_F9 -> Event.KEY_F9;
            case GLFW_KEY_F10 -> Event.KEY_F10;
            case GLFW_KEY_F11 -> Event.KEY_F11;
            case GLFW_KEY_F12 -> Event.KEY_F12;
            case GLFW_KEY_F13 -> Event.KEY_F13;
            case GLFW_KEY_F14 -> Event.KEY_F14;
            case GLFW_KEY_F15 -> Event.KEY_F15;
            case GLFW_KEY_KP_ENTER -> Event.KEY_NUMPADENTER;
            case GLFW_KEY_RIGHT_CONTROL -> Event.KEY_RCONTROL;
            case GLFW_KEY_RIGHT_ALT -> Event.KEY_RMENU;
            case GLFW_KEY_HOME -> Event.KEY_HOME;
            case GLFW_KEY_UP -> Event.KEY_UP;
            case GLFW_KEY_PAGE_UP -> Event.KEY_PRIOR;
            case GLFW_KEY_LEFT -> Event.KEY_LEFT;
            case GLFW_KEY_RIGHT -> Event.KEY_RIGHT;
            case GLFW_KEY_END -> Event.KEY_END;
            case GLFW_KEY_DOWN -> Event.KEY_DOWN;
            case GLFW_KEY_PAGE_DOWN -> Event.KEY_NEXT;
            case GLFW_KEY_INSERT -> Event.KEY_INSERT;
            case GLFW_KEY_DELETE -> Event.KEY_DELETE;
            default -> Event.KEY_NONE;
        };
    }
}
