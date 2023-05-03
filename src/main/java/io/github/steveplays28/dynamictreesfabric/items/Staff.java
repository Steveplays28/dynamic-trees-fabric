package io.github.steveplays28.dynamictreesfabric.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.ColorUtil;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


/**
 * Try the following in a command block to demonstrate the extra tag functionality. {@code /give @p
 * dynamictrees:staff{color:0x88FF00,code:"OUiVpPzkbtJ9uSRPbZP",read_only:1,tree:"dynamictrees:birch",max_uses:16,display:{Name:'[{"text":"Name","italic":false}]'}}}
 */
public class Staff extends Item {

	public final static String HANDLE = "handle";
	public final static String COLOR = "color";

	public final static String READ_ONLY = "read_only";
	public final static String TREE = "tree";
	public final static String CODE = "code";
	public final static String USES = "uses";
	public final static String MAX_USES = "max_uses";

	private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

	public Staff() {
		super(new Item.Settings().maxCount(1)
				.tab(DTRegistries.ITEM_GROUP));

		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 5.0, EntityAttributeModifier.Operation.ADDITION));
		builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.4, EntityAttributeModifier.Operation.ADDITION));
		this.attributeModifiers = builder.build();
	}


	@Override
	public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
		if (state.getBlock() instanceof BranchBlock || state.getBlock() instanceof TrunkShellBlock) {
			return 64.0f;
		}
		return super.getMiningSpeedMultiplier(stack, state);
	}

	@Override
	public boolean postMine(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (state.getBlock() instanceof BranchBlock || state.getBlock() instanceof TrunkShellBlock) {
			if (decUses(stack)) {
				stack.decrement(1);
			}
			return true;
		}
		return false;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		ItemStack heldStack = context.getPlayer().getStackInHand(context.getHand());

		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);

		BlockPos rootPos = TreeHelper.findRootNode(world, pos);
		TreePart treePart = TreeHelper.getTreePart(world.getBlockState(rootPos));

		// Get the code from a tree or rooty dirt and set it in the staff
		if (!isReadOnly(heldStack) && treePart.isRootNode()) {
			Species species = TreeHelper.getExactSpecies(world, rootPos);
			if (species.isValid()) {
				if (!context.getPlayer().isSneaking()) {
					String code = new JoCode(world, rootPos, context.getPlayer().getHorizontalFacing()).toString();
					setCode(heldStack, code);
					if (world.isClient) { // Make sure this doesn't run on the server
						MinecraftClient.getInstance().keyboard.setClipboard(code); // Put the code in the system clipboard to annoy everyone.
					}
				}
				setSpecies(heldStack, species);
				return ActionResult.SUCCESS;
			}
		}

		//Create a tree from right clicking on soil
		Species species = getSpecies(heldStack);
		if (species.isValid() && species.isAcceptableSoil(world, pos, state)) {
			species.getJoCode(getCode(heldStack)).setCareful(true).generate(world, world, species, pos, world.getBiome(pos), context.getPlayer().getHorizontalFacing(), 8, SafeChunkBounds.ANY, false);
			if (hasMaxUses(heldStack)) {
				if (decUses(heldStack)) {
					heldStack.decrement(1);//If the player is in creative this will have no effect.
				}
			} else {
				heldStack.decrement(1);//If the player is in creative this will have no effect.
			}
			return ActionResult.SUCCESS;
		}

		return ActionResult.FAIL;
	}

	@Override
	public boolean isItemBarVisible(ItemStack pStack) {
		return hasMaxUses(pStack);
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		int damage = getUses(stack) / getMaxUses(stack);
		return 1 - damage;
	}

	public boolean isReadOnly(ItemStack itemStack) {
		return itemStack.getOrCreateNbt().getBoolean(READ_ONLY);
	}

	public Staff setReadOnly(ItemStack itemStack, boolean readonly) {
		itemStack.getOrCreateNbt().putBoolean(READ_ONLY, readonly);
		return this;
	}

	public Staff setSpecies(ItemStack itemStack, Species species) {
		String name;

		if (species == Species.NULL_SPECIES) {
			name = "null";
		} else {
			name = species.getRegistryName().toString();
		}

		itemStack.getOrCreateNbt().putString(TREE, name);
		return this;
	}

	public Staff setCode(ItemStack itemStack, String code) {
		itemStack.getOrCreateNbt().putString(CODE, code);
		return this;
	}

	public Species getSpecies(ItemStack itemStack) {
		NbtCompound nbt = itemStack.getOrCreateNbt();

		if (nbt.contains(TREE)) {
			return TreeRegistry.findSpecies(nbt.getString(TREE));
		} else {
			Species species = TreeRegistry.findSpeciesSloppy("oak");
			setSpecies(itemStack, species);
			return species;
		}
	}

	public int getUses(ItemStack itemStack) {
		NbtCompound nbt = itemStack.getOrCreateNbt();

		if (nbt.contains(USES)) {
			return nbt.getInt(USES);
		} else {
			int uses = getMaxUses(itemStack);
			setUses(itemStack, uses);
			return uses;
		}

	}

	public Staff setUses(ItemStack itemStack, int value) {
		itemStack.getOrCreateNbt().putInt(USES, value);
		return this;
	}

	public int getMaxUses(ItemStack itemStack) {
		NbtCompound nbt = itemStack.getOrCreateNbt();

		if (nbt.contains(MAX_USES)) {
			return nbt.getInt(MAX_USES);
		}

		return 0;
	}

	public Staff setMaxUses(ItemStack itemStack, int value) {
		itemStack.getOrCreateNbt().putInt(MAX_USES, value);
		return this;
	}

	public boolean hasMaxUses(ItemStack itemStack) {
		return itemStack.getOrCreateNbt().contains(MAX_USES);
	}

	public boolean decUses(ItemStack itemStack) {
		int uses = Math.max(0, getUses(itemStack) - 1);
		setUses(itemStack, uses);
		return uses <= 0;
	}

	public int getColor(ItemStack itemStack, int tint) {
		final NbtCompound tag = itemStack.getOrCreateNbt();

		if (tint == 0) {
			int color = 0x005b472f; // Original brown wood color

			Species species = getSpecies(itemStack);

			if (tag.contains(HANDLE)) {
				try {
					color = ColorUtil.decodeARGB32(tag.getString(HANDLE));
				} catch (NumberFormatException e) {
					tag.remove(HANDLE);
				}
			} else if (species.isValid()) {
				color = species.getFamily().woodBarkColor;
			}

			return color;
		} else if (tint == 1) {
			int color = 0x0000FFFF; // Cyan crystal like Radagast the Brown's staff.

			if (tag.contains(COLOR)) {
				// Convert legacy string tag to int tag if tag type is String.
				if (tag.getType(COLOR) == NbtElement.STRING_TYPE) {
					this.tryConvertLegacyTag(tag);
				}
				color = tag.getInt(COLOR);
			}

			return color;
		}


		return 0xFFFFFFFF; // white
	}

	/**
	 * The {@link #COLOR} tag used to store a Hex String, such as {@code #FFFFFF}, but was recently changed to store an
	 * int instead. This attempts to convert the legacy tag to an int.
	 *
	 * @param tag The {@link NbtCompound} tag containing the {@link #COLOR} string.
	 * @deprecated This will no longer be necessary in 1.17.
	 */
	@Deprecated
	private void tryConvertLegacyTag(final NbtCompound tag) {
		final String color = tag.getString(COLOR);
		tag.remove(COLOR);

		try {
			tag.putInt(COLOR, ColorUtil.decodeARGB32(color));
		} catch (final NumberFormatException ignored) {
		}
	}

	public Staff setColor(ItemStack itemStack, int color) {
		itemStack.getOrCreateNbt().putInt(COLOR, color);
		return this;
	}

	public String getCode(ItemStack itemStack) {
		String code = "P";//Code of a sapling

		if (itemStack.getOrCreateNbt().contains(CODE)) {
			code = itemStack.getNbt().getString(CODE);
		} else {
			itemStack.getNbt().putString(CODE, code);
		}

		return code;
	}


@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		tooltip.add(Text.translatable("tooltip.dynamictrees.species", this.getSpecies(stack).getTextComponent()));
		tooltip.add(Text.translatable("tooltip.dynamictrees.jo_code", new JoCode(this.getCode(stack)).getTextComponent()));
	}

	/**
	 * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
	 */

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
	}

}
