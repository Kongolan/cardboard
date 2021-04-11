package org.cardboardpowered.impl.block;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import com.google.common.base.Preconditions;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

@SuppressWarnings("deprecation")
public class CardboardBanner extends CardboardBlockEntityState<BannerBlockEntity> implements Banner {

    private DyeColor base;
    private List<Pattern> patterns;

    public CardboardBanner(final Block block) {
        super(block, BannerBlockEntity.class);
    }

    public CardboardBanner(final Material material, final BannerBlockEntity te) {
        super(material, te);
    }

    @Override
    public void load(BannerBlockEntity banner) {
        super.load(banner);

        base = DyeColor.getByWoolData((byte) ((AbstractBannerBlock) this.data.getBlock()).getColor().getId());
        patterns = new ArrayList<Pattern>();

        // TODO: 1.17ify
      /*  if (banner.patternListTag != null) {
            for (int i = 0; i < banner.patternListTag.size(); i++) {
                NbtCompound p = (NbtCompound) banner.patternListTag.get(i);
                patterns.add(new Pattern(DyeColor.getByWoolData((byte) p.getInt("Color")), PatternType.getByIdentifier(p.getString("Pattern"))));
            }
        }*/
    }

    @Override
    public DyeColor getBaseColor() {
        return this.base;
    }

    @Override
    public void setBaseColor(DyeColor color) {
        Preconditions.checkArgument(color != null, "color");
        this.base = color;
    }

    @Override
    public List<Pattern> getPatterns() {
        return new ArrayList<Pattern>(patterns);
    }

    @Override
    public void setPatterns(List<Pattern> patterns) {
        this.patterns = new ArrayList<Pattern>(patterns);
    }

    @Override
    public void addPattern(Pattern pattern) {
        this.patterns.add(pattern);
    }

    @Override
    public Pattern getPattern(int i) {
        return this.patterns.get(i);
    }

    @Override
    public Pattern removePattern(int i) {
        return this.patterns.remove(i);
    }

    @Override
    public void setPattern(int i, Pattern pattern) {
        this.patterns.set(i, pattern);
    }

    @Override
    public int numberOfPatterns() {
        return patterns.size();
    }

    @Override
    public void applyTo(BannerBlockEntity banner) {
        super.applyTo(banner);

        banner.baseColor = net.minecraft.util.DyeColor.byId(base.getWoolData());
        NbtList newPatterns = new NbtList();

        for (Pattern p : patterns) {
            NbtCompound compound = new NbtCompound();
            compound.putInt("Color", p.getColor().getWoolData());
            compound.putString("Pattern", p.getPattern().getIdentifier());
            newPatterns.add(compound);
        }
        // TODO 1.17ify: banner.patternListTag = newPatterns;
    }

}