//  CoreData.swift
//
//  Created by Anthony Smith on 11/11/19.
//  Copyright © 2019 Seer Medical. All rights reserved.

import Foundation
import CoreData

@objc(OCCoreDataService)
class CoreDataService : NSObject {
    
    static let sharedManager = CoreDataService()
    
    @objc
    class func getSharedInstance() -> CoreDataService {
      return CoreDataService.sharedManager
    }
    
    private override init() {}
    
    lazy var persistentContainer: NSPersistentContainer = {

        let container = NSPersistentContainer(name: "Seer")
        container.loadPersistentStores(completionHandler: { (storeDescription, error) in
            if let error = error as NSError? {
                 fatalError("Unresolved error \(error), \(error.userInfo)")
             }
         })
         return container
     }()

    @objc
    func saveContext () {
         let context = persistentContainer.viewContext
         if context.hasChanges {
             do {
                 try context.save()
             } catch {
                 let nserror = error as NSError
                 fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
             }
         }
     }
    
    @objc
    func insertButtonData(userId: String, startTime: Double, createdAt: Date, timezone: Float)->ButtonData? {
        
        let managedContext = CoreDataService.sharedManager.persistentContainer.viewContext
        let entity = NSEntityDescription.entity(forEntityName: "ButtonData",
                                              in: managedContext)!
        let buttonData = NSManagedObject(entity: entity,
                                   insertInto: managedContext)
        
        buttonData.setValue(userId, forKeyPath: "user_id")
        buttonData.setValue(startTime, forKeyPath: "start_time")
        buttonData.setValue(createdAt, forKeyPath: "created_at")
        buttonData.setValue(timezone, forKeyPath: "timezone")
        
      do {
        try managedContext.save()
        return buttonData as? ButtonData
      } catch let error as NSError {
        print("Could not save. \(error), \(error.userInfo)")
        return nil
      }
    }
    
    @objc
    func fetchButtonData(userId: String) -> [Dictionary<String, Any>]?{
      
        var buttonList = [Dictionary<String, Any>]()
            
      let managedContext = CoreDataService.sharedManager.persistentContainer.viewContext

      let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: "ButtonData")
      fetchRequest.predicate = NSPredicate(format: "user_id == %@", userId)

      fetchRequest.returnsObjectsAsFaults = false
        
      let sort = NSSortDescriptor(key: "created_at", ascending: true)
      fetchRequest.fetchLimit = 100
      fetchRequest.sortDescriptors = [sort]

      do {
        let buttonData = try managedContext.fetch(fetchRequest)
        let buttonDataConverted = buttonData as! [ButtonData]
        
        let dateFormatter = DateFormatter()
        dateFormatter.timeZone = TimeZone(abbreviation: "GMT") //Set timezone that you want
        dateFormatter.locale = NSLocale.current
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm" //Specify your format that you want
        
        for button in buttonDataConverted{
            var buttonDictionary = Dictionary<String, Any>()
            buttonDictionary["created_at"] = dateFormatter.string(from: button.created_at!)
            buttonDictionary["start_time"] = button.start_time
            buttonDictionary["timezone"] = button.timezone
            buttonDictionary["user_id"] = button.user_id
            buttonList.append(buttonDictionary)
        }
        return buttonList
      } catch let error as NSError {
        print("Could not fetch. \(error), \(error.userInfo)")
        return nil
      }
    }
    
    @objc
    func deleteButtonData(userId: String){
      
      let managedContext = CoreDataService.sharedManager.persistentContainer.viewContext
      let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "ButtonData")
      fetchRequest.predicate = NSPredicate(format: "user_id == %@", userId)

      fetchRequest.returnsObjectsAsFaults = false
      
      let sort = NSSortDescriptor(key: "created_at", ascending: true)
      fetchRequest.fetchLimit = 100
      fetchRequest.sortDescriptors = [sort]
      let batchDeleteRequest = NSBatchDeleteRequest(fetchRequest: fetchRequest)
      
      do {  
        try managedContext.execute(batchDeleteRequest)
      } catch {
        print(error)
      }
      do {
        try managedContext.save()
      } catch {
      }
    }

    @objc
    func convertToJSONArray(moArray: [ButtonData]) -> Any {
      var jsonArray: [[String: Any]] = []
      for item in moArray {
          var dict: [String: Any] = [:]
          for attribute in item.entity.attributesByName {
              if let value = item.value(forKey: attribute.key) {
                  dict[attribute.key] = value
              }
          }
          jsonArray.append(dict)
      }
      return jsonArray
    }
}
