package fr.pantheonsorbonne.ufr27.miage.service;

import fr.pantheonsorbonne.ufr27.miage.dao.InventoryDAO;
import fr.pantheonsorbonne.ufr27.miage.dao.VillagerDAO;
import fr.pantheonsorbonne.ufr27.miage.dao.WishlistDAO;
import fr.pantheonsorbonne.ufr27.miage.dto.Merchandise;
import fr.pantheonsorbonne.ufr27.miage.dto.Product;
import fr.pantheonsorbonne.ufr27.miage.model.Villager;
import fr.pantheonsorbonne.ufr27.miage.model.Wishlist;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

@ApplicationScoped
public class ProductServiceImpl implements ProductService {

    final int priceHouse = 4000;

    private static final Logger LOGGER = Logger.getLogger("logger");

    @Inject
    BankingService bankingService;

    @Inject
    InventoryService inventoryService;

    @Inject
    WishlistDAO wishlistDAO;

    @Inject
    VillagerDAO villagerDAO;

    @Inject
    InventoryDAO inventoryDAO;

    @Inject
    HousingService housingService;

    @Inject
    LoaningService loaningService;

    @Override
    public boolean findLikedProduct(Product product, int idVillager) {
        Villager villager = villagerDAO.getVillager(idVillager);
        if (inventoryService.sufficientSpace(idVillager, 1)) {
            Wishlist wishlist = wishlistDAO.getWishlist(idVillager);
            LOGGER.info("Villager" + idVillager + " has enough space");
            if (wishlist.getShape().equals(product.getShape()) && Objects.equals(wishlist.getColor(), product.getColor()) && villager.getLevel() == product.getLevel()) {
                LOGGER.info("Villager " + idVillager + " likes this " + product.getName());
                if (bankingService.sufficientBalance(product.getPrice(), idVillager)) {
                    LOGGER.info("Villager " + idVillager + " has enough money to purchase this item");
                    return true;
                }
                else { LOGGER.warning("Villager " + idVillager + " doesn't have enough money for purchase"); }
            }
            else { LOGGER.info("Villager " + idVillager + " doesn't like this " + product.getName()); }
        }
        else { LOGGER.warning("Villager " + idVillager + " doesn't have enough space to purchase an item");

            if (!housingService.upgradeHouse(idVillager)) {
                LOGGER.info("Villager " + idVillager + " needs to ask for loan");
                loaningService.emitLoanRequest(idVillager, (villager.getLevel() + 1) * priceHouse);
            }
        }
        return false;
    }

    @Override
    public void purchaseProducts(Merchandise merchandise, int idVillager) {
        for (Product product : merchandise.getMerchandise()) {
            inventoryDAO.addToInventory(idVillager);
            bankingService.debitBankAccount(product.getPrice(), idVillager);
        }
    }

    @Override
    public Merchandise scavengeMerchandise(Merchandise merchandise, int idVillager) {
        Villager villager = villagerDAO.getVillager(idVillager);
        Collection<Product> likedProducts = new ArrayList<>();
        int purchaseTotal = 0;

        for(Product product : merchandise.getMerchandise()) {
            if(this.findLikedProduct(product, idVillager)) {
                likedProducts.add(product);
            }
        }



        while(!bankingService.sufficientBalance(purchaseTotal, idVillager)
                || !inventoryService.sufficientSpace(idVillager, likedProducts.size())) {
            Product mostExpensiveItem = (Product) likedProducts.toArray()[0];

            for (Product product : likedProducts)  {
                purchaseTotal=+product.getPrice();
                if (product.getPrice() > mostExpensiveItem.getPrice()) {
                    mostExpensiveItem = product;
                }
            }

            likedProducts.remove(mostExpensiveItem);
        }

        if (likedProducts.isEmpty()) {
            return null;
        }

        return new Merchandise(likedProducts);
    }

}
