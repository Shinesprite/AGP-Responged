package agp.andwhat5.battles;

import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.comm.ChatHandler;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public class TempTeamedParticipant extends PlayerParticipant {

    public TempTeamedParticipant(EntityPlayerMP p, EntityPixelmon... startingPixelmon) {
        super(p, startingPixelmon);
    }

    //    public PixelmonWrapper switchPokemon(PixelmonWrapper pw, UUID newPixelmonUUID) {
    @Override
    public PixelmonWrapper switchPokemon(PixelmonWrapper pw, UUID newPixelmonUUID) {
        double x = this.player.posX;
        double y = this.player.posY;
        double z = this.player.posZ;
        String beforeName = pw.getNickname();
        pw.beforeSwitch();
        if (!pw.isFainted() && !pw.nextSwitchIsMove) {
            ChatHandler.sendBattleMessage(this.player, "playerparticipant.enough", pw.getNickname());
            this.bc.sendToOthers("playerparticipant.withdrew", this, this.player.getDisplayName().getUnformattedText(), beforeName);
        }

        PixelmonWrapper newWrapper = null;

        for (PixelmonWrapper pixelmonWrapper : allPokemon) {
            if(pixelmonWrapper.getPokemonUUID().equals(newPixelmonUUID)) {
                newWrapper = pixelmonWrapper;
                break;
            }
        }

        if(newWrapper == null) {
            this.bc.sendToAll("Problem sending out Pokémon, cancelling battle. Please report this to AGP.");
            this.bc.endBattle(EnumBattleEndCause.FORCE);
            return null;
        }

        if (!this.bc.simulateMode) {
            pw.pokemon.retrieve();//catchInPokeball
            int slot = this.getStorage().getSlot(newPixelmonUUID);
            Pokemon newPixelmon;
            if (slot != -1) {
                newPixelmon = getStorage().get(slot);
                //newPixelmon.motionX = newPixelmon.motionY = newPixelmon.motionZ = 0.0D;
                //newPixelmon.setLocationAndAngles(x, y, z, this.player.rotationYaw, 0.0F);
            } else {
                newPixelmon = newWrapper.pokemon;
                getWorld().spawnEntity(newPixelmon.getOrSpawnPixelmon(null));
                //newPixelmon.motionX = newPixelmon.motionY = newPixelmon.motionZ = 0.0D;
                //newPixelmon.setLocationAndAngles(x, y, z, this.player.rotationYaw, 0.0F);
                //newPixelmon.releaseFromPokeball();
            }

            newWrapper.pokemon = newPixelmon;
        }

        newWrapper.battlePosition = pw.battlePosition;
        newWrapper.getBattleAbility().beforeSwitch(newWrapper);
        String newNickname = newWrapper.getNickname();
        ChatHandler.sendBattleMessage(this.player, "playerparticipant.go", newNickname);
        this.bc.sendToOthers("battlecontroller.sendout", this, this.player.getDisplayName().getUnformattedText(), newNickname);
        int index = this.controlledPokemon.indexOf(pw);
        this.controlledPokemon.set(index, newWrapper);
        this.bc.participants.forEach(BattleParticipant::updateOtherPokemon);
        newWrapper.afterSwitch();
        return newWrapper;
    }

    //TODO we need to return a custom storage i recon

    //Default code checks if the pw.entity is null, which won't work here as we need it to exist
    //So check if the pokemon is currently controlled by this battle instead
    @Override
    public PixelmonWrapper getRandomPartyPokemon() {
        List<PixelmonWrapper> choices = new ArrayList<>();
        PixelmonWrapper[] var2 = this.allPokemon;

        for (PixelmonWrapper pw : var2) {
            if (!pw.isFainted() && !this.controlledPokemon.contains(pw)) {
                choices.add(pw);
            }
        }

        return RandomHelper.getRandomElementFromList(choices);
    }

}