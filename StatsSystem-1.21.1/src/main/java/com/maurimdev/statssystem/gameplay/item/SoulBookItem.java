package com.maurimdev.statssystem.gameplay.item;

import com.maurimdev.statssystem.client.event.ClientEvents;
import com.maurimdev.statssystem.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class SoulBookItem extends Item implements ICurioItem {

    public static final String LECTERN_SOUL_BOOK_TAG = "statssystem:lectern_soul_book";

    public SoulBookItem(Properties properties) {
        super(properties);
    }

    /**
     * Crea un Written Book con tag especial para atriles
     * Muestra texto bonito cuando se lee, y al retirarlo se convertirá en Soul Book
     */
    public static ItemStack createSoulBookForLectern() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        // Agregar custom data con tag especial
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(LECTERN_SOUL_BOOK_TAG, true);
        book.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Crear el contenido del libro con texto bonito
        Filterable<String> title = Filterable.passThrough("Ancient Soul Book");
        String author = "???";

        // Crear páginas con texto formateado
        Filterable<Component> page1 = Filterable.passThrough(Component.literal(
            "§6§l═══════════════\n" +
            "  ANCIENT SOUL\n" +
            "      BOOK\n" +
            "§6§l═══════════════\n\n" +
            "§r§7This mystical tome\n" +
            "holds the power to\n" +
            "track and enhance\n" +
            "your abilities.\n\n" +
            "§8Legends say it was\n" +
            "created by ancient\n" +
            "scholars who sought\n" +
            "to understand the\n" +
            "essence of growth."
        ));

        Filterable<Component> page2 = Filterable.passThrough(Component.literal(
            "§6§lPowers:\n\n" +
            "§r§7• Track your stats\n" +
            "  and progression\n\n" +
            "• Choose between\n" +
            "  XP Mode or\n" +
            "  Practice Mode\n\n" +
            "• View detailed\n" +
            "  information about\n" +
            "  your character\n\n" +
            "§8Take this book to\n" +
            "begin your journey."
        ));

        WrittenBookContent bookContent = new WrittenBookContent(
            title,
            author,
            0,
            List.of(page1, page2),
            true
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, bookContent);

        return book;
    }

    /**
     * Verifica si un ItemStack es un libro de atril que debe convertirse en Soul Book
     */
    public static boolean isLecternSoulBook(ItemStack stack) {
        if (stack.getItem() != Items.WRITTEN_BOOK) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        return customData.copyTag().getBoolean(LECTERN_SOUL_BOOK_TAG);
    }

    /**
     * Convierte un Book & Quill de atril en Soul Book real
     */
    public static ItemStack convertToSoulBook(ItemStack lecternBook) {
        return new ItemStack(ModItems.SOUL_BOOK.get());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ClientEvents.openStatsScreen();
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // Usar traducciones en lugar de texto hardcodeado
        tooltipComponents.add(Component.translatable("tooltip.statssystem.soul_book.line1").withStyle(style -> style.withColor(0xAAAAAA)));
        tooltipComponents.add(Component.translatable("tooltip.statssystem.soul_book.line2").withStyle(style -> style.withColor(0xAAAAAA)));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("tooltip.statssystem.soul_book.line3").withStyle(style -> style.withColor(0xFFAA00)));
        tooltipComponents.add(Component.translatable("tooltip.statssystem.soul_book.line4").withStyle(style -> style.withColor(0x555555)));
    }

    // Métodos de ICurioItem

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // El Soul Book está equipado
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            // Solo mostrar mensaje si realmente se está equipando (no al cargar el juego)
            // Si prevStack está vacío Y el jugador tiene más de 1 tick de vida, significa que acaba de equipar
            if (prevStack.isEmpty() && player.tickCount > 20) {
                player.displayClientMessage(
                        Component.translatable("message.statssystem.soul_book.equipped")
                                .withStyle(style -> style.withColor(0xFFAA00)),
                        true
                );
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable("message.statssystem.soul_book.unequipped")
                            .withStyle(style -> style.withColor(0x555555)),
                    true
            );
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}