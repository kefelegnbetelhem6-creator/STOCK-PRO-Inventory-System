-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 28, 2026 at 04:18 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `inventory_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `address` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`id`, `name`, `phone`, `email`, `address`) VALUES
(1, 'aaa', '0970678766', 'DFGASGVCB@GMAIL.COM', NULL),
(3, 'sss', '0789898989', 'ass@gmail.com', NULL),
(4, 'safsdf', '0945454545', 'adfvhdvj@gmail.com', NULL),
(5, 'sss', '0789898989', 'ass@gmail.com', NULL),
(6, 'eyuu', '0789898754', 'eyuu', NULL),
(7, 'eyuu', '0789898754', 'eyuu@gmail.com', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `min_stock` int(11) DEFAULT 5
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`id`, `name`, `category`, `quantity`, `price`, `min_stock`) VALUES
(2, 'laptop', NULL, 12188, 12222, 5),
(3, 'laptop', NULL, 12188, 12222, 5),
(4, 'headset', NULL, 12000, 3000, 5),
(5, 'ssss', NULL, 0, 222, 5),
(6, 'pc', NULL, 0, 1, 5),
(7, 'lap', NULL, 12, 222, 5),
(9, 'fgfhhg', NULL, 2232, 56, 5),
(10, 'wqefwdasf', NULL, 8, 23, 5),
(11, 'qw', NULL, 4, 2333, 5),
(12, 'mobile', NULL, 123321, 120000, 78),
(13, 'iphone', NULL, 1200000, 200000, 67);

-- --------------------------------------------------------

--
-- Table structure for table `sales`
--

CREATE TABLE `sales` (
  `id` int(11) NOT NULL,
  `product_name` varchar(100) DEFAULT NULL,
  `customer_name` varchar(100) DEFAULT NULL,
  `quantity_sold` int(11) DEFAULT NULL,
  `total_price` double DEFAULT NULL,
  `sold_by` varchar(50) DEFAULT NULL,
  `sale_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sales`
--

INSERT INTO `sales` (`id`, `product_name`, `customer_name`, `quantity_sold`, `total_price`, `sold_by`, `sale_date`) VALUES
(1, 'laptop', NULL, 12, 146664, NULL, '2026-04-26 06:31:20'),
(2, 'qwww', NULL, 11, 132, NULL, '2026-04-26 06:42:38'),
(3, 'qwww', 'sss', 1, 12, 'admin', '2026-04-26 06:54:31'),
(4, 'pc', 'safsdf', 111111, 111111, 'admin', '2026-04-26 06:59:55'),
(5, 'wqefwdasf', 'sss', 4, 92, 'admin', '2026-04-26 07:01:25'),
(6, 'laptop', 'sss', 11, 134442, 'bbb', '2026-04-26 12:21:34'),
(7, 'laptop', 'aaa', 11, 134442, 'beta', '2026-04-26 18:24:09'),
(8, 'fgfhhg', 'safsdf', 1, 56, 'beta', '2026-04-26 20:11:58'),
(9, 'ssss', 'safsdf', 12, 2664, 'bbb', '2026-04-27 23:23:03'),
(10, 'mobile', 'aaa', 12, 1440000, 'bbb', '2026-04-28 01:13:46');

-- --------------------------------------------------------

--
-- Table structure for table `suppliers`
--

CREATE TABLE `suppliers` (
  `id` int(11) NOT NULL,
  `company_name` varchar(100) NOT NULL,
  `contact_person` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `suppliers`
--

INSERT INTO `suppliers` (`id`, `company_name`, `contact_person`, `phone`, `address`) VALUES
(1, 'Samsung Ethiopia', 'Abebe Kebede', '0676767676', NULL),
(2, 'apple', 'elu', '0989898989', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password`, `role`) VALUES
(8, 'bb', 'bbbcc@gmail.com', 'ee79976c9380d5e337fc1c095ece8c8f22f91f306ceeb161fa51fecede2c4ba1', 'user'),
(9, 'beta', 'beti@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'admin'),
(10, 'bbb', 'bbbb@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'user');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `sales`
--
ALTER TABLE `sales`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `suppliers`
--
ALTER TABLE `suppliers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `sales`
--
ALTER TABLE `sales`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `suppliers`
--
ALTER TABLE `suppliers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
