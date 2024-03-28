import { SortableItem } from './interfaces/sortable-item.interface';
import { ElementDto } from './modules/designer/dtos/element.dto';
import { BlueprintElementDto } from './modules/designer/dtos/blueprint-element.dto';

export abstract class AppUtils {

  static updateSorting<T extends SortableItem>(
    items: T[],
    sortingUpdate: {itemUuid: string, oldSorting: number, newSorting: number}
  ): T[] {
    const moveDown = sortingUpdate.newSorting < sortingUpdate.oldSorting;
    const itemsToUpdateMap = new Map<string, T>();

    items = AppUtils.sort<T>(items);

    // fix gaps in sorting due to deleting items
    for (let i = 0; i < items.length; i++) {
      if (items[i].sorting === i) {
        continue;
      }

      items[i].sorting = i;
      itemsToUpdateMap.set(items[i].uuid, items[i]);
    }

    for (const item of items) {
      if (item.uuid === sortingUpdate.itemUuid) {
        item.sorting = sortingUpdate.newSorting;
        itemsToUpdateMap.set(item.uuid, item);
        continue;
      }

      if (moveDown) {
        if (item.sorting >= sortingUpdate.oldSorting || item.sorting < sortingUpdate.newSorting) {
          continue;
        }
        item.sorting++;
      } else {
        if (item.sorting <= sortingUpdate.oldSorting || item.sorting > sortingUpdate.newSorting) {
          continue;
        }
        item.sorting--;
      }

      itemsToUpdateMap.set(item.uuid, item);
    }

    // https://stackoverflow.com/a/29514855 (accessed 23/11/2022, 21:02)
    return [...itemsToUpdateMap.values()];
  }

  static sort<T extends SortableItem>(items: T[]): T[] {
    return items.sort((itemA, itemB) => itemA.sorting - itemB.sorting);
  }

  static sortElementDtos<T extends ElementDto | BlueprintElementDto>(elementDtos: T[]): T[] {
    return elementDtos.sort(
      (elementA, elementB) => elementA.element.sorting - elementB.element.sorting
    );
  }

  static buildPreviewText(text: string, maxLength = 20, removeLineBreaks = true) {

    if (removeLineBreaks) {
      text = text.replace('\n', ' ');
    }

    if (text.length <= maxLength) {
      return text;
    }

    return text.substr(0, maxLength) + '...';

  }
}
