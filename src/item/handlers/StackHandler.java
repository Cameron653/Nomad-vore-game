package item.handlers;

import java.util.List;

import item.Item;
import item.instances.ItemStack;

public class StackHandler {

	public static boolean handleStacking(Item item, List<Item> items)
	{
		int amount=1;
		if (ItemStack.class.isInstance(item))
		{
			amount=((ItemStack)item).getCount();
		}
		if (item.canStack())
		{
			Item c = null;
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).canStack() &&
						items.get(i).getClass().getName().contains("Stack")) {
					ItemStack stack = (ItemStack) items.get(i);
					if (stack.getItem().getItem().equals(item.getItem().getItem())) {


						// add to stack
						stack.setCount(stack.getCount() + amount);
						return true;
					}
				} else {
					if (items.get(i).canStack() && items.get(i).getItem() == item.getItem()) {
						items.remove(i);
						ItemStack stack = new ItemStack(item.getItem(), 1 + amount);
						items.add(stack);
						return true;
						// remove this item
						// then build a stack with 2 blocks
					}
				}

			}
		}
		return false;
	}

}
