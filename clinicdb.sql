-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 18, 2025 at 06:30 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `clinicdb`
--

-- --------------------------------------------------------

--
-- Table structure for table `appointments`
--

CREATE TABLE `appointments` (
  `id` int(11) NOT NULL,
  `patient_id` int(11) DEFAULT NULL,
  `doctor_id` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `time` time DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `appointments`
--

INSERT INTO `appointments` (`id`, `patient_id`, `doctor_id`, `date`, `time`, `status`) VALUES
(1, 1, 1, '2025-07-01', '09:00:00', 'completed'),
(2, 2, 1, '2025-07-02', '11:00:00', 'confirmed'),
(3, 3, 2, '2025-07-03', '15:00:00', 'confirmed'),
(4, 1, 1, '2025-07-01', '09:00:00', 'completed'),
(5, 2, 1, '2025-07-02', '11:00:00', 'completed'),
(6, 3, 2, '2025-07-03', '15:00:00', 'confirmed'),
(7, 1, 1, '2025-06-29', '09:00:00', 'confirmed'),
(8, 1, 2, '2025-06-29', '09:00:00', 'completed'),
(9, 1, 2, '2025-06-29', '11:00:00', 'completed'),
(10, 1, 2, '2025-06-30', '09:00:00', 'confirmed'),
(11, 1, 2, '2025-07-18', '09:00:00', 'Rescheduled'),
(12, 1, 1, '2025-07-17', '09:00:00', 'cancelled'),
(13, 1, 1, '2025-07-19', '09:00:00', 'completed'),
(14, 1, 1, '2025-07-20', '09:00:00', 'confirmed');

-- --------------------------------------------------------

--
-- Table structure for table `doctors`
--

CREATE TABLE `doctors` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `specialization` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `doctors`
--

INSERT INTO `doctors` (`id`, `name`, `email`, `password`, `specialization`) VALUES
(1, 'Dr.John', 'john21@gmail.com', 'abc123', 'Eye'),
(2, 'Dr.David', 'david20@gmail.com', 'abc123', 'General Health'),
(3, 'Dr. Smith', 'dr.smith@clinic.com', 'doctor123', 'Cardiology'),
(4, 'Dr. Johnson', 'dr.johnson@clinic.com', 'doctor123', 'Neurology');

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE `patients` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`id`, `name`, `email`, `password`, `date_of_birth`, `phone`) VALUES
(1, 'kessigan', 'kessigan@gmail.com', 'abc123', '2003-10-09', '01111514367'),
(2, 'John Doe', 'john@example.com', 'patient123', '1980-05-15', '1234567890'),
(3, 'Jane Smith', 'jane@example.com', 'patient123', '1990-08-21', '9876543210');

-- --------------------------------------------------------

--
-- Table structure for table `treatments`
--

CREATE TABLE `treatments` (
  `id` int(11) NOT NULL,
  `appointment_id` int(11) DEFAULT NULL,
  `diagnosis` text DEFAULT NULL,
  `treatment_type` varchar(100) DEFAULT NULL,
  `medication` text DEFAULT NULL,
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `treatments`
--

INSERT INTO `treatments` (`id`, `appointment_id`, `diagnosis`, `treatment_type`, `medication`, `notes`) VALUES
(1, 5, 'Cough', 'Medication', 'Paracetamol', 'Not feeling well'),
(2, 1, 'Common cold', 'Medication', 'Paracetamol', 'Rest for 2 days'),
(3, 2, 'Sprained ankle', 'Physical Therapy', 'Ibuprofen', 'Use ice pack'),
(4, 13, 'Sakit Cinta', 'Medication', 'Paracetamol', 'try test');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doctors`
--
ALTER TABLE `doctors`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `treatments`
--
ALTER TABLE `treatments`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `appointments`
--
ALTER TABLE `appointments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `doctors`
--
ALTER TABLE `doctors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `patients`
--
ALTER TABLE `patients`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `treatments`
--
ALTER TABLE `treatments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
