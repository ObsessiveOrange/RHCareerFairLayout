//
//  RHCareerFairLayout.swift
//  RHCareerFairLayout
//
//  Created by Benedict Seng Sum Wong on 8/5/15.
//  Copyright (c) 2015 ObsessiveOrange. All rights reserved.
//

import UIKit

extension UIColor {
    
    convenience init(hex: UInt) {
        
        let components = (
            A: CGFloat((hex >> 24) & 0xff) / 255,
            R: CGFloat((hex >> 16) & 0xff) / 255,
            G: CGFloat((hex >> 08) & 0xff) / 255,
            B: CGFloat((hex >> 00) & 0xff) / 255
        )
        
        self.init(red: components.R, green: components.G, blue: components.B, alpha: components.A)
        
    }
    
}

class RHCareerFairLayout: NSObject {

    static let dbName: String = "RHCareerFairLayout.sqlite";
    static let dbVersion: Int = 1;
    static var database : FMDatabase?;
    
    static var color_tabText : UIColor = UIColor(hex: 0x99ffffff);
    static var color_tabTextSelected : UIColor = UIColor(hex: 0xffffffff);
    static var color_primary : UIColor = UIColor(hex: 0xff800000);
    static var color_primaryDark : UIColor = UIColor(hex: 0xff600000);
    static var color_accent : UIColor = UIColor(hex: 0xffbbbbbb);
    static var color_accentLight : UIColor = UIColor(hex: 0xffdddddd);
    static var color_black : UIColor = UIColor(hex: 0xff000000);
    static var color_green : UIColor = UIColor(hex: 0xff00ff00);
    static var color_yellow : UIColor = UIColor(hex: 0xffffff00);
    
    static var companyDetailSegueIdentifier : String = "CompanyDetail";
    
    static var data : NSDictionary?;
    
    static var aboutString: String = "The RH Career Fair App is designed to help students, faculty and recruiters navigate around career fairs, without needing to resort to unwieldy paper maps. This app, the Android app, website, and server were designed by student Benedict Wong.";
    
}
