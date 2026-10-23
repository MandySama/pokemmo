package top.totodile.pokemmo.client.ui.common;

import com.badlogic.gdx.*;
import com.badlogic.gdx.input.*;
import de.matthiasmann.twl.*;
import java.util.function.BooleanSupplier;

/** Same native Android text-field bridge already used by PokedexWindow. */
public final class PlayerTextInput implements AutoCloseable {
    private final boolean mobile;
    private final BooleanSupplier active;
    private EditField current;
    public PlayerTextInput(boolean mobile,BooleanSupplier active){this.mobile=mobile;this.active=active;}
    public EditField field(int maximum,boolean multiline){
        var field=new EditField(){@Override public boolean handleEvent(Event event){
            boolean result=super.handleEvent(event);if(event.getType()==Event.Type.MOUSE_BTNDOWN)open(this,maximum,multiline);return result;
        }};field.setMaxTextLength(maximum);field.setMultiLine(multiline);return field;
    }
    private void open(EditField field,int maximum,boolean multiline){
        if(!mobile||!active.getAsBoolean()||Gdx.app.getType()!=Application.ApplicationType.Android||current==field)return;
        if(current!=null){Gdx.input.closeTextInputField(false,confirmed->{open(field,maximum,multiline);return true;});return;}
        current=field;String initial=field.getText();int cursor=field.getCursorPos();
        Gdx.input.openTextInputField(new NativeInputConfiguration().setType(Input.OnscreenKeyboardType.Default)
            .setPreventCorrection(true).setMultiLine(multiline).setMaxLength(maximum)
            .setTextInputWrapper(new TextInputWrapper(){
                public String getText(){return initial;}
                public int getSelectionStart(){return cursor;}
                public int getSelectionEnd(){return cursor;}
                public void writeResults(String text,int start,int end){if(active.getAsBoolean()){field.setText(text);field.setSelection(Math.min(start,text.length()),Math.min(end,text.length()));}}
            }).setCloseCallback(confirmed->{current=null;return false;}));
    }
    @Override public void close(){if(current!=null){Gdx.input.closeTextInputField(false);current=null;}}
}
